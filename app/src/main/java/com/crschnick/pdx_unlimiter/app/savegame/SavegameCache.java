package com.crschnick.pdx_unlimiter.app.savegame;

import com.crschnick.pdx_unlimiter.app.game.*;
import com.crschnick.pdx_unlimiter.app.installation.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.installation.PdxuInstallation;
import com.crschnick.pdx_unlimiter.app.util.JsonHelper;
import com.crschnick.pdx_unlimiter.eu4.savegame.Eu4Savegame;
import com.crschnick.pdx_unlimiter.eu4.savegame.SavegameInfo;
import com.crschnick.pdx_unlimiter.eu4.savegame.SavegameParseException;
import com.crschnick.pdx_unlimiter.eu4.savegame.Eu4RawSavegame;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.file.PathUtils;
import org.apache.commons.lang3.function.FailableFunction;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.StreamSupport;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public abstract class SavegameCache<I extends SavegameInfo, E extends GameCampaignEntry<I>, C extends GameCampaign<E>> {

    public static final Eu4SavegameCache EU4_CACHE = new Eu4SavegameCache();
    public static final Hoi4SavegameCache HOI4_CACHE = new Hoi4SavegameCache();

    public static final Set<SavegameCache<?,?,?>> CACHES = Set.of(EU4_CACHE, HOI4_CACHE);
    private volatile Queue<E> toLoad = new ConcurrentLinkedQueue<>();
    private String name;
    private Path path;
    private volatile ObservableSet<C> campaigns = FXCollections.synchronizedObservableSet(FXCollections.observableSet(new TreeSet<>()));

    public SavegameCache(String name) {
        this.name = name;
        this.path = PdxuInstallation.getInstance().getSavegameLocation().resolve(name);
        addChangeListeners();
    }

    private void init() throws IOException {
        FileUtils.forceMkdir(getPath().toFile());
        FileUtils.forceMkdir(getBackupPath().toFile());
        if (Files.exists(getPath().resolve(getDataFile()))) {
            importDataFromConfig(p -> Files.newInputStream(getPath().resolve(p)));
        }

        Thread loader = new Thread(() -> {
            while (true) {
                if (toLoad.peek() != null) {
                    loadEntry(toLoad.poll());
                }

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        loader.setDaemon(true);
        loader.setPriority(Thread.MIN_PRIORITY);
        loader.start();
    }

    public static void loadData() throws IOException {
        for (SavegameCache cache : CACHES) {
            cache.init();
        }
    }

    public static void saveData() throws IOException {
        for (SavegameCache cache : CACHES) {
            FailableFunction<Path,OutputStream,IOException> f =
                    (p) -> Files.newOutputStream(cache.getPath().resolve(p));
            cache.exportDataToConfig(f);
        }
    }

    public static void importSavegameCache(Path in) {
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(in.toFile());
        } catch (IOException e) {
            ErrorHandler.handleException(e);
            return;
        }

        for (SavegameCache cache : SavegameCache.CACHES) {
            cache.importSavegameCache(zipFile);
        }

        try {
            zipFile.close();
        } catch (IOException e) {
            ErrorHandler.handleException(e);
        }
    }

    public static void exportSavegameCache(Path out) {
        ZipOutputStream zipFile = null;
        try {
            zipFile = new ZipOutputStream(new FileOutputStream(out.toString()));
        } catch (FileNotFoundException e) {
            ErrorHandler.handleException(e);
            return;
        }

        for (SavegameCache cache : SavegameCache.CACHES) {
            cache.exportSavegameCache(zipFile);
        }

        try {
            zipFile.close();
        } catch (IOException e) {
            ErrorHandler.handleException(e);
        }
    }

    public static void exportSavegameDirectory(Path out) {
        ZipOutputStream zipFile = null;
        try {
            zipFile = new ZipOutputStream(new FileOutputStream(out.toString()));
        } catch (FileNotFoundException e) {
            ErrorHandler.handleException(e);
            return;
        }

        for (SavegameCache cache : SavegameCache.CACHES) {
            cache.exportSavegameDirectory(zipFile);
        }

        try {
            zipFile.close();
        } catch (IOException e) {
            ErrorHandler.handleException(e);
        }
    }

    private void addChangeListeners() {
        this.campaigns.addListener((SetChangeListener<? super C>) cc -> {
            if (cc.wasRemoved()) {
                return;
            }

            C c = cc.getElementAdded();
            c.getSavegames().addListener((SetChangeListener<? super E>) (change) -> {
                boolean isNewEntry = change.wasAdded() && change.getElementAdded().infoProperty().isNotNull().get();
                boolean wasRemoved = change.wasRemoved();
                if (isNewEntry || wasRemoved) updateCampaignProperties(c);
            });
        });
    }

    protected abstract void updateCampaignProperties(C c);

    public void importDataFromConfig(FailableFunction<Path,InputStream,IOException> in) throws IOException {
        ObjectMapper o = new ObjectMapper();
        JsonNode node = o.readTree(in.apply(Path.of("campaigns.json")).readAllBytes());

        JsonNode c = node.required("campaigns");
        for (int i = 0; i < c.size(); i++) {
            String name = c.get(i).required("name").textValue();
            UUID id = UUID.fromString(c.get(i).required("uuid").textValue());
            Instant lastDate = Instant.parse(c.get(i).required("lastPlayed").textValue());
            campaigns.add(readCampaign(c.get(i), name, id, lastDate));
        }

        for (C campaign : campaigns) {
             try {
                 JsonNode campaignNode = o.readTree(in.apply(
                         Path.of(campaign.getCampaignId().toString()).resolve("campaign.json"))
                         .readAllBytes());
                 StreamSupport.stream(campaignNode.required("entries").spliterator(), false).forEach(entryNode -> {
                     UUID eId = UUID.fromString(entryNode.required("uuid").textValue());
                     String name = Optional.ofNullable(entryNode.get("name")).map(JsonNode::textValue).orElse(null);
                     String checksum = entryNode.required("checksum").textValue();
                     campaign.add(readEntry(entryNode, name, eId, checksum));
                 });
             } catch (Exception e) {
                 ErrorHandler.handleException(e, "Could not load campaign config of " + campaign.getName());
             }
        }
    }

    protected abstract E readEntry(JsonNode node, String name, UUID uuid, String checksum);
    protected abstract C readCampaign(JsonNode node, String name, UUID uuid, Instant lastPlayed);

    public void exportDataToConfig(FailableFunction<Path,OutputStream,IOException> out) throws IOException {
        ObjectNode n = JsonNodeFactory.instance.objectNode();
        ArrayNode c = n.putArray("campaigns");;
        for (C campaign : getCampaigns()) {
            ObjectNode campaignFileNode = JsonNodeFactory.instance.objectNode();
            ArrayNode entries = campaignFileNode.putArray("entries");
            campaign.getSavegames().stream()
                    .map(entry -> {
                        ObjectNode node = JsonNodeFactory.instance.objectNode()
                                .put("name", entry.getName())
                                .put("checksum", entry.getChecksum())
                                .put("uuid", entry.getUuid().toString());
                        writeEntry(node, entry);
                        return node;
                    })
                    .forEach(entries::add);
            JsonHelper.write(campaignFileNode,
                    out.apply(Path.of(campaign.getCampaignId().toString(), "campaign.json")));

            ObjectNode campaignNode = JsonNodeFactory.instance.objectNode()
                    .put("name", campaign.getName())
                    .put("lastPlayed", campaign.getLastPlayed().toString())
                    .put("uuid", campaign.getCampaignId().toString());
            writeCampaign(campaignNode, campaign);
            c.add(campaignNode);
        }

        JsonHelper.write(n, out.apply(Path.of("campaigns.json")));
    }

    protected abstract void writeEntry(ObjectNode node, E e);
    protected abstract void writeCampaign(ObjectNode node, C c);

    public void importSavegameCache(ZipFile zipFile) {
    }

    private void exportSavegameDirectory(ZipOutputStream out) {
        Set<String> names = new HashSet<>();
        for (C c : getCampaigns()) {
            for (E e : c.getSavegames()) {
                String name = getEntryName(e);
                if (names.contains(name)) {
                    name += "_" + UUID.randomUUID().toString();
                }
                names.add(name);
                try {
                    compressFileToZipfile(
                            getPath(e).resolve("savegame." + name).toFile(),
                            PdxuInstallation.getInstance().getSavegameLocation()
                                    .relativize(getPath()).resolve(name + "." + name).toString(),
                            out);
                } catch (IOException ioException) {
                    ErrorHandler.handleException(ioException);
                }
            }
        }
    }

    private void exportSavegameCache(ZipOutputStream out) {
    }

    private void compressFileToZipfile(File file, String name, ZipOutputStream out) throws IOException {
        ZipEntry entry = new ZipEntry(name);
        out.putNextEntry(entry);

        FileInputStream in = new FileInputStream(file);
        IOUtils.copy(in, out);
        in.close();
    }

    public synchronized void delete(C c) {
        if (!this.campaigns.contains(c)) {
            return;
        }

        Path campaignPath = path.resolve(c.getCampaignId().toString());
        try {
            FileUtils.deleteDirectory(campaignPath.toFile());
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (GameIntegration.globalSelectedCampaignProperty().get() == c) {
            GameIntegration.current().selectCampaign(null);
        }

        this.campaigns.remove(c);
    }

    public synchronized E addNewEntry(UUID campainUuid, UUID entryUuid, String checksum, I i) {
        if (this.getCampaign(campainUuid).isEmpty()) {
            this.campaigns.add(createCampaign(i));
        }

        C c = this.getCampaign(campainUuid).get();
        E e = createEntry(entryUuid, checksum, i);
        c.add(e);

        GameIntegration.selectIntegration(GameIntegration.getForSavegameCache(this));
        GameIntegration.current().selectEntry(e);
        return e;
    }

    protected abstract C createCampaign(I info);
    protected abstract E createEntry(UUID uuid, String checksum, I info);

    public synchronized boolean updateSavegameData(E e) {
        Path p = getPath(e);
        Path s = p.resolve("savegame." + name);
        try {
            PathUtils.delete(p.resolve("data.zip"));
        } catch (IOException ioException) {
            ErrorHandler.handleException(ioException);
            return false;
        }

        try {
            writeSavegameData(s, p.resolve("data.zip"));
        } catch (Exception ex) {
            ErrorHandler.handleException(ex);
            return false;
        }
        return true;
    }

    protected abstract void writeSavegameData(Path savegame, Path out) throws Exception;

    public synchronized C getCampaign(E e) {
        var campaign = campaigns.stream().filter(c -> c.getSavegames().contains(e)).findAny();
        return campaign.orElseThrow(() -> new IllegalArgumentException("Could not find campaign for entry " + e.getName()));
    }

    public synchronized void delete(E e) {
        C c = getCampaign(e);
        if (!this.campaigns.contains(c) || !c.getSavegames().contains(e)) {
            return;
        }

        Path campaignPath = path.resolve(c.getCampaignId().toString());
        try {
            FileUtils.deleteDirectory(campaignPath.resolve(e.getUuid().toString()).toFile());
        } catch (IOException ex) {
            ErrorHandler.handleException(ex);
        }

        if (GameIntegration.globalSelectedEntryProperty().get() == e) {
            GameIntegration.current().selectEntry(null);
        }
        c.getSavegames().remove(e);
        if (c.getSavegames().size() == 0) {
            delete(c);
        }
    }

    public void loadEntryAsync(E e) {
        if (e.infoProperty().isNull().get()) {
            this.toLoad.add(e);
        }
    }

    private synchronized void loadEntry(E e) {
        if (e.infoProperty().isNotNull().get()) {
            return;
        }

        try {
            if (needsUpdate(e)) {
                if (!updateSavegameData(e)) {
                    return;
                }
            }
            I info = loadInfo(getPath(e).resolve("data.zip"));
            e.infoProperty().set(info);
        } catch (Exception exception) {
            ErrorHandler.handleException(exception);
        }
    }

    protected abstract boolean needsUpdate(E e) throws Exception;

    protected abstract I loadInfo(Path p) throws Exception;

    public synchronized Path getPath(E e) {
        Path campaignPath = path.resolve(getCampaign(e).getCampaignId().toString());
        return campaignPath.resolve(e.getUuid().toString());
    }

    public synchronized Optional<C> getCampaign(UUID uuid) {
        for (C c : campaigns) {
            if (c.getCampaignId().equals(uuid)) {
                return Optional.of(c);
            }
        }
        return Optional.empty();
    }

    public synchronized String getFileName(E e) {
        return getCampaign(e).getName() + " " + e.getName().replace(":", ".") + "." + name;
    }

    public synchronized void exportSavegame(E e, Path destPath) throws IOException {
        Path srcPath = getPath(e).resolve("savegame." + name);
        FileUtils.copyFile(srcPath.toFile(), destPath.toFile(), false);
        destPath.toFile().setLastModified(Instant.now().toEpochMilli());
    }

    public synchronized void importSavegame(Path file) {
        try {
            importSavegameData(file);
        } catch (Exception e) {
            ErrorHandler.handleException(e);
        }
    }

    protected abstract void importSavegameData(Path file) throws Exception;

    public String getEntryName(E e) {
        String cn = getCampaign(e).getName();
        String en = e.getName();
        return cn + " (" + en + ")";
    }

    public Path getPath() {
        return path;
    }

    public Path getDataFile() {
        return Path.of("campaigns.json");
    }

    public Path getBackupPath() {
        return PdxuInstallation.getInstance().getSavegameBackupLocation().resolve(name);
    }

    public ObservableSet<C> getCampaigns() {
        return campaigns;
    }

    public static class Status {
        private Type type;
        private String path;

        public Status(Type type, String path) {
            this.type = type;
            this.path = path;
        }

        public Type getType() {
            return type;
        }

        public String getPath() {
            return path;
        }

        public static enum Type {
            IMPORTING,
            LOADING,
            UPDATING,
            DELETING,
            IMPORTING_ARCHIVE,
            EXPORTING_ARCHIVE
        }
    }
}

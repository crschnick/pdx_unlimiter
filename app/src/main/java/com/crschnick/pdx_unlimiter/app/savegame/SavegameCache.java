package com.crschnick.pdx_unlimiter.app.savegame;

import com.crschnick.pdx_unlimiter.app.PdxuApp;
import com.crschnick.pdx_unlimiter.app.game.*;
import com.crschnick.pdx_unlimiter.app.installation.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.installation.PdxuInstallation;
import com.crschnick.pdx_unlimiter.app.util.JsonHelper;
import com.crschnick.pdx_unlimiter.eu4.Eu4IntermediateSavegame;
import com.crschnick.pdx_unlimiter.eu4.Eu4SavegameInfo;
import com.crschnick.pdx_unlimiter.eu4.SavegameInfo;
import com.crschnick.pdx_unlimiter.eu4.SavegameParseException;
import com.crschnick.pdx_unlimiter.eu4.parser.Eu4Savegame;
import com.crschnick.pdx_unlimiter.eu4.parser.GameDate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.function.FailableFunction;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.StreamSupport;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public abstract class SavegameCache<I extends SavegameInfo, E extends GameCampaignEntry<? extends SavegameInfo>, C extends GameCampaign<E>> {

    public static final Eu4SavegameCache EU4_CACHE = new Eu4SavegameCache();

    public static final Set<SavegameCache> CACHES = Set.of(EU4_CACHE);
    private volatile Queue<E> toLoad = new ConcurrentLinkedQueue<>();
    private String name;
    private Path path;
    private ObjectProperty<Optional<Status>> status = new SimpleObjectProperty<>(Optional.empty());
    private volatile ObservableSet<C> campaigns = FXCollections.synchronizedObservableSet(FXCollections.observableSet(new HashSet<>()));

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
            cache.exportDataToConfig(p -> Files.newOutputStream(cache.getPath().resolve(p)));
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
                boolean isNewEntry = change.wasAdded() && change.getElementAdded().getInfo().isPresent();
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
            String id = c.get(i).required("uuid").textValue();
            String tag = c.get(i).required("tag").textValue();
            String lastDate = c.get(i).required("date").textValue();
            String lastPlayed = c.get(i).required("lastPlayed").textValue();
            addFromConfig(tag, name, id, GameDate.fromString(lastDate), Timestamp.valueOf(lastPlayed));
        }

        for (C campaign : campaigns) {
            JsonNode campaignNode = o.readTree(in.apply(
                    Path.of(campaign.getCampaignId().toString()).resolve("campaign.json"))
                    .readAllBytes());
            StreamSupport.stream(campaignNode.required("entries").spliterator(), false).forEach(entryNode -> {
                UUID eId = UUID.fromString(entryNode.required("uuid").textValue());
                String name = entryNode.required("name").textValue();
                String tag = entryNode.required("tag").textValue();
                GameDate date = GameDate.fromString(entryNode.required("date").textValue());
                campaign.add(new Eu4CampaignEntry(new SimpleStringProperty(name),
                        eId, new SimpleObjectProperty<>(Optional.empty()), tag, date));
            });
        }
    }

    public void exportDataToConfig(FailableFunction<Path,OutputStream,IOException> out) throws IOException {
        ObjectNode n = JsonNodeFactory.instance.objectNode();
        ArrayNode c = n.putArray("campaigns");;
        for (C campaign : getCampaigns()) {
            ObjectNode campaignFileNode = JsonNodeFactory.instance.objectNode();
            ArrayNode entries = campaignFileNode.putArray("entries");
            campaign.getSavegames().stream()
                    .map(entry -> JsonNodeFactory.instance.objectNode()
                            .put("name", entry.getName())
                            .put("tag", entry.getTag())
                            .put("uuid", entry.getUuid().toString())
                            .put("date", entry.getDate().toString()))
                    .forEach(entries::add);
            JsonHelper.write(campaignFileNode,
                    out.apply(Path.of(campaign.getCampaignId().toString(), "campaign.json")));

            ObjectNode campaignNode = JsonNodeFactory.instance.objectNode()
                    .put("name", campaign.getName())
                    .put("lastPlayed", campaign.getLastPlayed().toString())
                    .put("tag", campaign.getTag())
                    .put("uuid", campaign.getCampaignId().toString())
                    .put("date", campaign.getDate().toString());
            c.add(campaignNode);
        }

        JsonHelper.write(n, out.apply(Path.of("campaigns.json")));
    }

    private synchronized void addFromConfig(String tag, Optional<String> name, String uuid, GameDate date, Timestamp lastPlayed) {
        Eu4Campaign c = createCampaign(name,)new Eu4Campaign(new SimpleObjectProperty<>(lastPlayed),
                new SimpleStringProperty(name),
                UUID.fromString(uuid), new SimpleStringProperty(tag), new SimpleObjectProperty<>(date));
        this.campaigns.add(c);
    }

    public void importSavegameCache(ZipFile zipFile) {
    }

    private void exportSavegameDirectory(ZipOutputStream out) {
        statusProperty().setValue(Optional.of(new Status(Status.Type.EXPORTING_ARCHIVE, out.toString())));
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
                            getPath(e).resolve("savegame.eu4").toFile(),
                            PdxuInstallation.getInstance().getSavegameLocation()
                                    .relativize(getPath()).resolve(name + ".eu4").toString(),
                            out);
                } catch (IOException ioException) {
                    ErrorHandler.handleException(ioException);
                }
            }
        }
        statusProperty().setValue(Optional.empty());
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
        this.campaigns.remove(c);
    }

    public synchronized void addNewEntry(Optional<String> name, UUID campainUuid, UUID entryUuid, I i) {
        if (this.getCampaign(campainUuid).isEmpty()) {
            this.campaigns.add(createCampaign(name, i));
        }

        C c = this.getCampaign(campainUuid).get();
        E e = createEntry(entryUuid, i);
        c.add(e);
    }

    protected abstract C createCampaign(Optional<String> name, I info);
    protected abstract E createEntry(UUID uuid, I info);

    public synchronized void updateSavegameData(E e) {
        status.setValue(Optional.of(new Status(Status.Type.UPDATING, getEntryName(e))));

        Path p = getPath(e);
        Path s = p.resolve("savegame.eu4");
        Eu4Savegame save = null;
        try {
            save = Eu4Savegame.fromFile(s);
        } catch (IOException ex) {
            ErrorHandler.handleException(ex);
            status.setValue(Optional.empty());
            return;
        }

        Eu4IntermediateSavegame is = null;
        try {
            is = Eu4IntermediateSavegame.fromSavegame(save);
        } catch (SavegameParseException ex) {
            ErrorHandler.handleException(ex);
            status.setValue(Optional.empty());
            return;
        }

        for (File f : p.toFile().listFiles((f) -> !f.toPath().equals(s))) {
            if (!f.delete()) {
                try {
                    throw new IOException("Couldn't delete file " + f.toString());
                } catch (IOException ioException) {
                    ErrorHandler.handleException(ioException);
                    status.setValue(Optional.empty());
                    return;
                }
            }
        }

        try {
            is.write(p.resolve("data.zip"), true);
        } catch (IOException ex) {
            ErrorHandler.handleException(ex);
            status.setValue(Optional.empty());
            return;
        }

        status.setValue(Optional.empty());
    }

    public synchronized C getCampaign(E e) {
        return campaigns.stream().filter(c -> c.getSavegames().contains(e)).findAny().get();
    }

    public synchronized void delete(E e) {
        C c = getCampaign(e);
        if (!this.campaigns.contains(c) || !c.getSavegames().contains(e)) {
            return;
        }

        status.setValue(Optional.of(new Status(Status.Type.DELETING, getEntryName(e))));

        Path campaignPath = path.resolve(c.getCampaignId().toString());
        try {
            FileUtils.deleteDirectory(campaignPath.resolve(e.getUuid().toString()).toFile());
        } catch (IOException ex) {
            ErrorHandler.handleException(ex);
        }

        c.getSavegames().remove(e);
        if (c.getSavegames().size() == 0) {
            delete(c);
        }

        status.setValue(Optional.empty());
    }

    public void loadEntryAsync(E e) {
        if (e.infoProperty().isNull().get()) {
            this.toLoad.add(e);
        }
    }

    private synchronized void loadEntry(E e) {
        if (e.infoProperty().isNull().get()) {
            return;
        }

        try {
            if (needsUpdate(e)) {
                updateSavegameData(e);
            }
            loadEntryData(e);
        } catch (Exception exception) {
            ErrorHandler.handleException(exception);
        }
    }

    protected abstract boolean needsUpdate(E e) throws Exception;

    protected abstract void loadEntryData(E e) throws Exception;

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
        return getCampaign(e).getName() + " " + e.getName() + ".eu4";
    }

    public synchronized Optional<Path> exportSavegame(E e, Path destPath) {
        Path srcPath = getPath(e).resolve("savegame.eu4");
        try {
            FileUtils.copyFile(srcPath.toFile(), destPath.toFile(), false);
            return Optional.of(destPath);
        } catch (IOException ioException) {
            ErrorHandler.handleException(ioException);
        }
        return Optional.empty();
    }

    public synchronized void importSavegame(Optional<String> name, Path file) {
        status.setValue(Optional.of(new Status(Status.Type.IMPORTING,
                GameInstallation.EU4.getUserDirectory().relativize(file).toString())));

        Eu4IntermediateSavegame is = null;
        Eu4SavegameInfo e = null;
        try {
            Eu4Savegame save = Eu4Savegame.fromFile(file);
            is = Eu4IntermediateSavegame.fromSavegame(save);
            e = Eu4SavegameInfo.fromSavegame(is);
        } catch (Exception ex) {
            ErrorHandler.handleException(ex);
            status.setValue(Optional.empty());
            return;
        }

        UUID saveUuid = UUID.randomUUID();
        UUID uuid = e.getCampaignUuid();
        Path campaignPath = path.resolve(uuid.toString());
        Path entryPath = campaignPath.resolve(saveUuid.toString());

        try {
            FileUtils.forceMkdir(entryPath.toFile());
            is.write(entryPath.resolve("data.zip"), true);
            FileUtils.copyFile(file.toFile(), getBackupPath().resolve(file.getFileName()).toFile());
            FileUtils.moveFile(file.toFile(), entryPath.resolve("savegame.eu4").toFile());

            this.addNewEntry(name, uuid, saveUuid, e.g);
        } catch (Exception ex) {
            ErrorHandler.handleException(ex);
            status.setValue(Optional.empty());
            return;
        }

        status.setValue(Optional.empty());
    }

    public String getEntryName(E e) {
        String cn = getCampaign(e).getName();
        String en = e.getName();
        return cn + " (" + en + ")";
    }

    public Optional<Status> getStatus() {
        return status.get();
    }

    public ObjectProperty<Optional<Status>> statusProperty() {
        return status;
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

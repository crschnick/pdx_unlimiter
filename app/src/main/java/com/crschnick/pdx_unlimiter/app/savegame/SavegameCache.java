package com.crschnick.pdx_unlimiter.app.savegame;

import com.crschnick.pdx_unlimiter.app.game.GameCampaign;
import com.crschnick.pdx_unlimiter.app.game.GameCampaignEntry;
import com.crschnick.pdx_unlimiter.app.game.GameIntegration;
import com.crschnick.pdx_unlimiter.app.installation.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.installation.PdxuInstallation;
import com.crschnick.pdx_unlimiter.app.installation.Settings;
import com.crschnick.pdx_unlimiter.app.util.JsonHelper;
import com.crschnick.pdx_unlimiter.eu4.data.GameDate;
import com.crschnick.pdx_unlimiter.eu4.data.GameDateType;
import com.crschnick.pdx_unlimiter.eu4.savegame.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.file.PathUtils;
import org.apache.commons.lang3.function.FailableFunction;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public abstract class SavegameCache<
        R extends RawSavegame,
        S extends Savegame,
        T,
        I extends SavegameInfo<T>> {

    public static final Eu4SavegameCache EU4_CACHE = new Eu4SavegameCache();
    public static final Hoi4SavegameCache HOI4_CACHE = new Hoi4SavegameCache();
    public static final StellarisSavegameCache STELLARIS_CACHE = new StellarisSavegameCache();
    public static final Ck3SavegameCache CK3_CACHE = new Ck3SavegameCache();

    public static final Set<SavegameCache<?, ?, ?, ?>> CACHES = Set.of(EU4_CACHE, HOI4_CACHE, STELLARIS_CACHE, CK3_CACHE);
    private volatile Queue<GameCampaignEntry<T,I>> toLoad = new ConcurrentLinkedQueue<>();

    private BooleanProperty loading = new SimpleBooleanProperty(false);
    private String fileEnding;
    private String name;
    private GameDateType dateType;
    private Path path;
    private volatile ObservableSet<GameCampaign<T,I>> campaigns = FXCollections.synchronizedObservableSet(
            FXCollections.observableSet(new HashSet<>()));
    private volatile Map<GameCampaignEntry<T,I>, S> loadedSavegames = Collections.synchronizedMap(new LinkedHashMap<>());

    public SavegameCache(String name, String fileEnding, GameDateType dateType) {
        this.name = name;
        this.fileEnding = fileEnding;
        this.dateType = dateType;
        this.path = PdxuInstallation.getInstance().getSavegameLocation().resolve(name);
        addChangeListeners();
    }

    public BooleanProperty loadingProperty() {
        return loading;
    }

    public static void loadData() throws IOException {
        for (SavegameCache cache : CACHES) {
            cache.init();
        }
    }

    public static void saveData() throws IOException {
        for (SavegameCache cache : CACHES) {
            FailableFunction<Path, OutputStream, IOException> f =
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

    private void init() throws IOException {
        FileUtils.forceMkdir(getPath().toFile());
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
        loader.setName("Savegame loader");
        loader.setDaemon(true);
        loader.start();
    }

    private void addChangeListeners() {
        this.campaigns.addListener((SetChangeListener<? super GameCampaign<T,I>>) cc -> {
            if (cc.wasRemoved()) {
                return;
            }

            GameCampaign<T,I> c = cc.getElementAdded();
            c.getEntries().addListener((SetChangeListener<? super GameCampaignEntry<T,I>>) (change) -> {
                boolean isNewEntry = change.wasAdded() && change.getElementAdded().infoProperty().isNotNull().get();
                boolean wasRemoved = change.wasRemoved();
                if (isNewEntry || wasRemoved) updateCampaignProperties(c);
            });
        });
    }

    private void updateCampaignProperties(GameCampaign<T,I>  c) {
        c.getEntries().stream()
                .filter(s -> s.infoProperty().isNotNull().get())
                .map(s -> s.getInfo().getDate()).min(Comparator.naturalOrder())
                .ifPresent(d -> c.dateProperty().setValue(d));

        c.getEntries().stream()
                .filter(s -> s.infoProperty().isNotNull().get())
                .min(Comparator.naturalOrder())
                .ifPresent(e -> c.tagProperty().setValue(e.getInfo().getTag()));
    }

    public void importDataFromConfig(FailableFunction<Path, InputStream, IOException> in) throws IOException {
        ObjectMapper o = new ObjectMapper();
        JsonNode node = o.readTree(in.apply(Path.of("campaigns.json")).readAllBytes());

        JsonNode c = node.required("campaigns");
        for (int i = 0; i < c.size(); i++) {
            String name = c.get(i).required("name").textValue();
            GameDate date = dateType.fromString(c.get(i).required("date").textValue());
            UUID id = UUID.fromString(c.get(i).required("uuid").textValue());
            if (!Files.isDirectory(getPath().resolve(id.toString()))) {
                continue;
            }

            Instant lastDate = Instant.parse(c.get(i).required("lastPlayed").textValue());
            campaigns.add(readCampaign(c.get(i), name, id, lastDate, date));
        }

        for (GameCampaign<T,I>  campaign : campaigns) {
            try {
                JsonNode campaignNode = o.readTree(in.apply(
                        Path.of(campaign.getCampaignId().toString()).resolve("campaign.json"))
                        .readAllBytes());
                StreamSupport.stream(campaignNode.required("entries").spliterator(), false).forEach(entryNode -> {
                    UUID eId = UUID.fromString(entryNode.required("uuid").textValue());
                    String name = Optional.ofNullable(entryNode.get("name")).map(JsonNode::textValue).orElse(null);
                    GameDate date = dateType.fromString(entryNode.required("date").textValue());
                    String checksum = entryNode.required("checksum").textValue();
                    campaign.add(readEntry(entryNode, name, eId, checksum, date));
                });
            } catch (Exception e) {
                ErrorHandler.handleException(e, "Could not load campaign config of " + campaign.getName());
            }
        }
    }

    protected abstract GameCampaignEntry<T,I>  readEntry(JsonNode node, String name, UUID uuid, String checksum, GameDate date);

    protected abstract GameCampaign<T,I> readCampaign(JsonNode node, String name, UUID uuid, Instant lastPlayed, GameDate date);

    public void exportDataToConfig(FailableFunction<Path, OutputStream, IOException> out) throws IOException {
        ObjectNode n = JsonNodeFactory.instance.objectNode();
        ArrayNode c = n.putArray("campaigns");
        ;
        for (GameCampaign<T,I>  campaign : getCampaigns()) {
            ObjectNode campaignFileNode = JsonNodeFactory.instance.objectNode();
            ArrayNode entries = campaignFileNode.putArray("entries");
            campaign.getEntries().stream()
                    .map(entry -> {
                        ObjectNode node = JsonNodeFactory.instance.objectNode()
                                .put("name", entry.getName())
                                .put("date", entry.getDate().toString())
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
                    .put("date", campaign.getDate().toString())
                    .put("lastPlayed", campaign.getLastPlayed().toString())
                    .put("uuid", campaign.getCampaignId().toString());
            writeCampaign(campaignNode, campaign);
            c.add(campaignNode);
        }

        JsonHelper.write(n, out.apply(Path.of("campaigns.json")));
    }

    protected abstract void writeEntry(ObjectNode node, GameCampaignEntry<T,I> e);

    protected abstract void writeCampaign(ObjectNode node, GameCampaign<T,I> c);

    public void importSavegameCache(ZipFile zipFile) {
    }

    private void exportSavegameDirectory(ZipOutputStream out) {
        Set<String> names = new HashSet<>();
        for (GameCampaign<T,I> c : getCampaigns()) {
            for (GameCampaignEntry<T,I> e : c.getEntries()) {
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

    public synchronized void delete(GameCampaign<T,I> c) {
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

    public synchronized GameCampaignEntry<T,I> addNewEntry(UUID campainUuid, UUID entryUuid, String checksum, I i, S savegame) {
        GameCampaignEntry<T,I> e = createEntry(entryUuid, checksum, i);
        if (this.getCampaign(campainUuid).isEmpty()) {
            var newCampaign = createNewCampaignForEntry(e);
            this.campaigns.add(newCampaign);
        }

        GameCampaign<T,I> c = this.getCampaign(campainUuid).get();
        c.add(e);

        GameIntegration.selectIntegration(GameIntegration.getForSavegameCache(this));
        GameIntegration.<T,I>current().selectEntry(e);
        updateLoadedSavegames(e, savegame);
        return e;
    }

    protected abstract GameCampaign<T,I> createNewCampaignForEntry(GameCampaignEntry<T,I> entry);

    protected abstract GameCampaignEntry<T,I> createEntry(UUID uuid, String checksum, I info);

    public synchronized boolean updateSavegameData(GameCampaignEntry<T,I> e) {
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

    private void writeSavegameData(Path savegame, Path out) throws Exception {
        R save = loadRaw(savegame);
        S sg = loadDataFromRaw(save);
        sg.write(out, true);
    }

    public synchronized boolean contains(GameCampaignEntry<?,?> e) {
        return campaigns.stream()
                .anyMatch(c -> c.getEntries().stream().anyMatch(ce -> ce.getUuid().equals(e.getUuid())));
    }

    public synchronized GameCampaign<T,I> getCampaign(GameCampaignEntry<T,I>  e) {
        var campaign = campaigns.stream()
                .filter(c -> c.getEntries().stream().anyMatch(ce -> ce.getUuid().equals(e.getUuid())))
                .findAny();
        return campaign.orElseThrow(() -> new IllegalArgumentException("Could not find campaign for entry " + e.getName()));
    }

    public synchronized void delete(GameCampaignEntry<T,I> e) {
        GameCampaign<T,I> c = getCampaign(e);
        if (!this.campaigns.contains(c) || !c.getEntries().contains(e)) {
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
        c.getEntries().remove(e);
        if (c.getEntries().size() == 0) {
            delete(c);
        }
    }

    public final Optional<S> loadDataForEntry(GameCampaignEntry<T,I> entry) {
        if (loadedSavegames.containsKey(entry)) {
            return Optional.ofNullable(loadedSavegames.get(entry));
        }

        try {
            return Optional.ofNullable(updateLoadedSavegames(entry));
        } catch (Exception e) {
            ErrorHandler.handleException(e);
            return Optional.empty();
        }
    }


    private S updateLoadedSavegames(GameCampaignEntry<T,I> entry) throws Exception {
        S s = loadDataFromFile(getPath(entry).resolve("data.zip"));
        updateLoadedSavegames(entry, s);
        return s;
    }

    private void updateLoadedSavegames(GameCampaignEntry<T,I> entry, S savegame) {
        if (loadedSavegames.size() >= Settings.getInstance().getMaxLoadedSavegames()) {
            GameCampaignEntry<T,I> first = loadedSavegames.keySet().iterator().next();
            loadedSavegames.remove(first);
            LoggerFactory.getLogger(SavegameCache.class).debug("Unloaded savegame data of entry " + getEntryName(first));
        }
        ;
        loadedSavegames.put(entry, savegame);
        LoggerFactory.getLogger(SavegameCache.class).debug("Loaded savegame data of entry " + getEntryName(entry));
    }

    public void loadEntryAsync(GameCampaignEntry<T,I> e) {
        if (e.infoProperty().isNull().get()) {
            LoggerFactory.getLogger(SavegameCache.class).debug("Added entry " + getEntryName(e) + " to loader queue");
            this.toLoad.add(e);
        }
    }

    private synchronized void loadEntry(GameCampaignEntry<T,I> e) {
        LoggerFactory.getLogger(SavegameCache.class).debug("Starting to load entry " + getEntryName(e));
        if (e.infoProperty().isNotNull().get()) {
            return;
        }

        try {
            if (needsUpdate(e)) {
                LoggerFactory.getLogger(SavegameCache.class).debug("Updating entry " + getEntryName(e));
                if (!updateSavegameData(e)) {
                    LoggerFactory.getLogger(SavegameCache.class).debug("Update failed for entry " + getEntryName(e));
                    return;
                }
            }
            S savegame = updateLoadedSavegames(e);
            I info = loadInfo(savegame);
            e.infoProperty().set(info);
            LoggerFactory.getLogger(SavegameCache.class).debug("Loaded entry " + getEntryName(e));
        } catch (Exception exception) {
            ErrorHandler.handleException(exception);
        }
    }

    protected abstract boolean needsUpdate(GameCampaignEntry<T,I> e);

    protected abstract I loadInfo(S data) throws Exception;

    protected abstract R loadRaw(Path p) throws Exception;

    protected abstract S loadDataFromFile(Path p) throws Exception;
    protected abstract S loadDataFromRaw(R raw) throws Exception;

    public synchronized Path getPath(GameCampaignEntry<T,I> e) {
        Path campaignPath = path.resolve(getCampaign(e).getCampaignId().toString());
        return campaignPath.resolve(e.getUuid().toString());
    }

    public synchronized Optional<GameCampaign<T,I>> getCampaign(UUID uuid) {
        for (GameCampaign<T,I> c : campaigns) {
            if (c.getCampaignId().equals(uuid)) {
                return Optional.of(c);
            }
        }
        return Optional.empty();
    }

    public synchronized String getFileName(GameCampaignEntry<T,I> e) {
        return getCampaign(e).getName() + " " + e.getName().replace(":", ".") + "." + fileEnding;
    }

    public synchronized void exportSavegame(GameCampaignEntry<T,I> e, Path destPath) throws IOException {
        Path srcPath = getPath(e).resolve("savegame." + fileEnding);
        FileUtils.forceMkdirParent(destPath.toFile());
        FileUtils.copyFile(srcPath.toFile(), destPath.toFile(), false);
        destPath.toFile().setLastModified(Instant.now().toEpochMilli());
    }

    public synchronized boolean importSavegame(Path file) {
        loading.setValue(true);
        try {
            importSavegameData(file);
            saveData();
            loading.setValue(false);
            return true;
        } catch (Exception e) {
            ErrorHandler.handleException(e);
            loading.setValue(false);
            return false;
        }
    }

    private String getDataFileName() {
        return "data.zip";
    }

    private String getSaveFileName() {
        return "savegame." + fileEnding;
    }

    private void importSavegameData(Path file) throws Exception {
        R rawSavegame = loadRaw(file);
        S savegame = loadDataFromRaw(rawSavegame);
        I info = loadInfo(savegame);

        UUID uuid = info.getCampaignUuid();

        final Optional<GameCampaignEntry<T, I>>[] exists = new Optional[]{Optional.empty()};
        getCampaigns().stream()
                .filter(c -> c.getCampaignId().equals(uuid))
                .findAny().ifPresent(c -> {
                    exists[0] = (c.getEntries().stream()
                            .filter(ch -> ch.getChecksum().equals(rawSavegame.getFileChecksum())))
                            .findAny();
        });
        if (exists[0].isPresent()) {
            loadEntry(exists[0].get());
            GameIntegration.selectIntegration(GameIntegration.getForSavegameCache(this));
            GameIntegration.<T,I>current().selectEntry(exists[0].get());
            return;
        }

        UUID saveUuid = UUID.randomUUID();
        Path campaignPath = getPath().resolve(uuid.toString());
        Path entryPath = campaignPath.resolve(saveUuid.toString());

        FileUtils.forceMkdir(entryPath.toFile());
        savegame.write(entryPath.resolve(getDataFileName()), true);
        FileUtils.copyFile(file.toFile(), entryPath.resolve(getSaveFileName()).toFile());
        this.addNewEntry(uuid, saveUuid, rawSavegame.getFileChecksum(), info, savegame);
    }

    public String getEntryName(GameCampaignEntry<T,I>  e) {
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

    public int indexOf(GameCampaign<?,?> c) {
        var list = new ArrayList<GameCampaign<T,I>>(getCampaigns());
        list.sort(Comparator.comparing(GameCampaign::getLastPlayed));
        Collections.reverse(list);
        return list.indexOf(c);
    }

    public Stream<GameCampaign<T,I>> campaignStream() {
        var list = new ArrayList<GameCampaign<T,I>>(getCampaigns());
        list.sort(Comparator.comparing(GameCampaign::getLastPlayed));
        Collections.reverse(list);
        return list.stream();
    }

    public ObservableSet<GameCampaign<T,I> > getCampaigns() {
        return campaigns;
    }

    public String getFileEnding() {
        return fileEnding;
    }
}

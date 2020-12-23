package com.crschnick.pdx_unlimiter.app.savegame;

import com.crschnick.pdx_unlimiter.app.game.GameCampaign;
import com.crschnick.pdx_unlimiter.app.game.GameCampaignEntry;
import com.crschnick.pdx_unlimiter.app.game.GameInstallation;
import com.crschnick.pdx_unlimiter.app.game.GameIntegration;
import com.crschnick.pdx_unlimiter.app.installation.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.installation.PdxuInstallation;
import com.crschnick.pdx_unlimiter.app.installation.TaskExecutor;
import com.crschnick.pdx_unlimiter.app.util.JsonHelper;
import com.crschnick.pdx_unlimiter.app.util.RakalyHelper;
import com.crschnick.pdx_unlimiter.core.data.GameDate;
import com.crschnick.pdx_unlimiter.core.data.GameDateType;
import com.crschnick.pdx_unlimiter.core.parser.Node;
import com.crschnick.pdx_unlimiter.core.savegame.SavegameInfo;
import com.crschnick.pdx_unlimiter.core.savegame.SavegameParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.file.PathUtils;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public abstract class SavegameCache<
        T,
        I extends SavegameInfo<T>> {

    public static Eu4SavegameCache EU4;
    public static Hoi4SavegameCache HOI4;
    public static StellarisSavegameCache STELLARIS;
    public static Ck3SavegameCache CK3;
    public static Set<SavegameCache<?, ?>> ALL;


    private String fileEnding;
    private String name;
    private GameDateType dateType;
    private Path path;
    private SavegameParser parser;
    private volatile ObservableSet<GameCampaign<T, I>> campaigns = FXCollections.synchronizedObservableSet(
            FXCollections.observableSet(new HashSet<>()));

    public SavegameCache(String name, String fileEnding, GameDateType dateType, SavegameParser parser) {
        this.name = name;
        this.parser = parser;
        this.fileEnding = fileEnding;
        this.dateType = dateType;
        this.path = PdxuInstallation.getInstance().getSavegameLocation().resolve(name);
        addChangeListeners();
    }

    public static void init() {
        if (GameInstallation.EU4 != null) {
            EU4 = new Eu4SavegameCache();
        }
        if (GameInstallation.HOI4 != null) {
            HOI4 = new Hoi4SavegameCache();
        }
        if (GameInstallation.STELLARIS != null) {
            STELLARIS = new StellarisSavegameCache();
        }
        if (GameInstallation.CK3 != null) {
            CK3 = new Ck3SavegameCache();
        }
        ALL = Stream.of(EU4, HOI4, STELLARIS, CK3)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        for (SavegameCache<?, ?> cache : ALL) {
            try {
                cache.loadData();
            } catch (IOException e) {
                ErrorHandler.handleException(e);
            }
        }
    }

    public static void reset() {
        for (SavegameCache<?, ?> cache : ALL) {
            try {
                cache.saveData();
            } catch (IOException e) {
                ErrorHandler.handleException(e);
            }
        }

        EU4 = null;
        HOI4 = null;
        STELLARIS = null;
        CK3 = null;
        ALL = Set.of();
    }

    private void addChangeListeners() {
        this.campaigns.addListener((SetChangeListener<? super GameCampaign<T, I>>) cc -> {
            if (cc.wasRemoved()) {
                return;
            }

            GameCampaign<T, I> c = cc.getElementAdded();
            c.getEntries().addListener((SetChangeListener<? super GameCampaignEntry<T, I>>) (change) -> {
                boolean isNewEntry = change.wasAdded() && change.getElementAdded().infoProperty().isNotNull().get();
                boolean wasRemoved = change.wasRemoved();
                if (isNewEntry || wasRemoved) updateCampaignProperties(c);
            });
        });
    }

    private void updateCampaignProperties(GameCampaign<T, I> c) {
        c.getEntries().stream()
                .filter(s -> s.infoProperty().isNotNull().get())
                .map(s -> s.getInfo().getDate()).min(Comparator.naturalOrder())
                .ifPresent(d -> c.dateProperty().setValue(d));

        c.getEntries().stream()
                .filter(s -> s.infoProperty().isNotNull().get())
                .min(Comparator.naturalOrder())
                .ifPresent(e -> c.tagProperty().setValue(e.getInfo().getTag()));
    }

    public void loadData() throws IOException {
        Files.createDirectories(getPath());
        if (!Files.exists(getDataFile())) {
            return;
        }

        InputStream in = Files.newInputStream(getDataFile());
        ObjectMapper o = new ObjectMapper();
        JsonNode node = o.readTree(in.readAllBytes());

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

        for (GameCampaign<T, I> campaign : campaigns) {
            try {
                InputStream campaignIn = Files.newInputStream(
                        getPath().resolve(campaign.getCampaignId().toString()).resolve("campaign.json"));
                JsonNode campaignNode = o.readTree(campaignIn.readAllBytes());
                StreamSupport.stream(campaignNode.required("entries").spliterator(), false).forEach(entryNode -> {
                    UUID eId = UUID.fromString(entryNode.required("uuid").textValue());
                    String name = Optional.ofNullable(entryNode.get("name")).map(JsonNode::textValue).orElse(null);
                    GameDate date = dateType.fromString(entryNode.required("date").textValue());
                    String checksum = entryNode.required("checksum").textValue();
                    campaign.add(readEntry(entryNode, name, eId, checksum, date));
                });
            } catch (Exception e) {
                ErrorHandler.handleException(e, "Could not load campaign config of " + campaign.getName(), null);
            }
        }
    }

    protected abstract GameCampaignEntry<T, I> readEntry(JsonNode node, String name, UUID uuid, String checksum, GameDate date);

    protected abstract GameCampaign<T, I> readCampaign(JsonNode node, String name, UUID uuid, Instant lastPlayed, GameDate date);

    private void saveData() throws IOException {
        ObjectNode n = JsonNodeFactory.instance.objectNode();
        ArrayNode c = n.putArray("campaigns");

        for (GameCampaign<T, I> campaign : getCampaigns()) {
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

            Path cFile = getPath()
                    .resolve(campaign.getCampaignId().toString()).resolve("campaign.json");
            Path backupCFile = getPath()
                    .resolve(campaign.getCampaignId().toString()).resolve("campaign_old.json");
            if (Files.exists(cFile)) {
                Files.copy(cFile, backupCFile, StandardCopyOption.REPLACE_EXISTING);
            }
            OutputStream out = Files.newOutputStream(cFile);
            JsonHelper.write(campaignFileNode, out);

            ObjectNode campaignNode = JsonNodeFactory.instance.objectNode()
                    .put("name", campaign.getName())
                    .put("date", campaign.getDate().toString())
                    .put("lastPlayed", campaign.getLastPlayed().toString())
                    .put("uuid", campaign.getCampaignId().toString());
            writeCampaign(campaignNode, campaign);
            c.add(campaignNode);
        }

        if (Files.exists(getDataFile())) {
            Files.copy(getDataFile(), getBackupDataFile(), StandardCopyOption.REPLACE_EXISTING);
        }
        OutputStream out = Files.newOutputStream(getDataFile());
        JsonHelper.write(n, out);
    }

    protected abstract void writeEntry(ObjectNode node, GameCampaignEntry<T, I> e);

    protected abstract void writeCampaign(ObjectNode node, GameCampaign<T, I> c);

    public synchronized void delete(GameCampaign<T, I> c) {
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
            GameIntegration.selectCampaign(null);
        }

        this.campaigns.remove(c);
    }

    public synchronized GameCampaignEntry<T, I> addNewEntry(UUID campainUuid, UUID entryUuid, String checksum, I i) {
        GameCampaignEntry<T, I> e = createEntry(entryUuid, checksum, i);
        if (this.getCampaign(campainUuid).isEmpty()) {
            var newCampaign = createNewCampaignForEntry(e);
            this.campaigns.add(newCampaign);
        }

        GameCampaign<T, I> c = this.getCampaign(campainUuid).get();
        c.add(e);

        GameIntegration.selectIntegration(GameIntegration.getForSavegameCache(this));
        GameIntegration.selectEntry(e);
        return e;
    }

    protected abstract GameCampaign<T, I> createNewCampaignForEntry(GameCampaignEntry<T, I> entry);

    protected abstract GameCampaignEntry<T, I> createEntry(UUID uuid, String checksum, I info);

    public synchronized boolean updateSavegameData(GameCampaignEntry<T, I> e) {
        Path p = getPath(e);
        Path s = p.resolve("savegame." + name);
        try {
            PathUtils.delete(p.resolve("data." + fileEnding));
        } catch (IOException ioException) {
            ErrorHandler.handleException(ioException);
            return false;
        }

        try {
            writeSavegameData(s, p.resolve("data." + fileEnding));
        } catch (Exception ex) {
            ErrorHandler.handleException(ex);
            return false;
        }
        return true;
    }

    private void writeSavegameData(Path savegame, Path out) throws Exception {
        byte[] melted = RakalyHelper.meltSavegame(savegame);
        Files.write(out, melted);
    }

    public synchronized boolean contains(GameCampaignEntry<?, ?> e) {
        return campaigns.stream()
                .anyMatch(c -> c.getEntries().stream().anyMatch(ce -> ce.getUuid().equals(e.getUuid())));
    }

    public synchronized GameCampaign<T, I> getCampaign(GameCampaignEntry<T, I> e) {
        var campaign = campaigns.stream()
                .filter(c -> c.getEntries().stream().anyMatch(ce -> ce.getUuid().equals(e.getUuid())))
                .findAny();
        return campaign.orElseThrow(() -> new IllegalArgumentException("Could not find campaign for entry " + e.getName()));
    }

    public synchronized void delete(GameCampaignEntry<T, I> e) {
        GameCampaign<T, I> c = getCampaign(e);
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
            GameIntegration.selectEntry(null);
        }
        c.getEntries().remove(e);
        if (c.getEntries().size() == 0) {
            delete(c);
        }
    }

    public void loadEntryAsync(GameCampaignEntry<T, I> e) {
        if (e.infoProperty().isNull().get()) {
            TaskExecutor.getInstance().submitTask(() -> {
                LoggerFactory.getLogger(SavegameCache.class).debug("Loading entry " + getEntryName(e));
                try {
                    loadEntry(e);
                } catch (Exception exception) {
                    ErrorHandler.handleException(exception);
                }
            }, false);
        }
    }

    private synchronized void loadEntry(GameCampaignEntry<T, I> e) throws Exception {
        LoggerFactory.getLogger(SavegameCache.class).debug("Starting to load entry " + getEntryName(e));
        if (e.infoProperty().isNotNull().get()) {
            return;
        }

        var file = getPath(e).resolve("savegame." + fileEnding);
        byte[] content = Files.readAllBytes(file);
        if (parser.isBinaryFormat(content)) {
            content = RakalyHelper.meltSavegame(file);
        }
        var node = parser.parse(content);
        I info = loadInfo(node);
        e.infoProperty().set(info);
        LoggerFactory.getLogger(SavegameCache.class).debug("Loaded entry " + getEntryName(e));
    }

    protected abstract I loadInfo(Node n) throws Exception;

    public synchronized Path getPath(GameCampaignEntry<T, I> e) {
        Path campaignPath = path.resolve(getCampaign(e).getCampaignId().toString());
        return campaignPath.resolve(e.getUuid().toString());
    }

    public synchronized Optional<GameCampaign<T, I>> getCampaign(UUID uuid) {
        for (GameCampaign<T, I> c : campaigns) {
            if (c.getCampaignId().equals(uuid)) {
                return Optional.of(c);
            }
        }
        return Optional.empty();
    }

    public synchronized String getFileName(GameCampaignEntry<T, I> e) {
        return getCampaign(e).getName() + " " + e.getName().replace(":", ".") + "." + fileEnding;
    }

    public synchronized void exportSavegame(GameCampaignEntry<T, I> e, Path destPath) throws IOException {
        Path srcPath = getPath(e).resolve("savegame." + fileEnding);
        FileUtils.forceMkdirParent(destPath.toFile());
        FileUtils.copyFile(srcPath.toFile(), destPath.toFile(), false);
        destPath.toFile().setLastModified(Instant.now().toEpochMilli());
    }

    synchronized boolean importSavegame(Path file) {
        try {
            importSavegameData(file);
            saveData();
            return true;
        } catch (Exception e) {
            ErrorHandler.handleException(e, "Could not import " + name + " savegame", file);
            return false;
        }
    }

    private String getSaveFileName() {
        return "savegame." + fileEnding;
    }

    private void importSavegameData(Path file) throws Exception {
        byte[] content = Files.readAllBytes(file);

        var checksum = parser.checksum(content);
        var exists = getCampaigns().stream().flatMap(GameCampaign::entryStream)
                .filter(ch -> ch.getChecksum().equals(checksum))
                .findAny();
        if (exists.isPresent()) {
            loadEntry(exists.get());
            GameIntegration.selectEntry(exists.get());
            return;
        }

        if (parser.isBinaryFormat(content)) {
            content = RakalyHelper.meltSavegame(file);
        }

        Node node = parser.parse(content);
        I info = loadInfo(node);
        UUID uuid = info.getCampaignUuid();

        UUID saveUuid = UUID.randomUUID();
        Path campaignPath = getPath().resolve(uuid.toString());
        Path entryPath = campaignPath.resolve(saveUuid.toString());

        FileUtils.forceMkdir(entryPath.toFile());
        FileUtils.copyFile(file.toFile(), entryPath.resolve(getSaveFileName()).toFile());
        this.addNewEntry(uuid, saveUuid, checksum, info);
    }

    public String getEntryName(GameCampaignEntry<T, I> e) {
        String cn = getCampaign(e).getName();
        String en = e.getName();
        return cn + " (" + en + ")";
    }

    public Path getPath() {
        return path;
    }

    public Path getDataFile() {
        return getPath().resolve("campaigns.json");
    }

    public Path getBackupDataFile() {
        return getPath().resolve("campaigns_old.json");
    }

    public int indexOf(GameCampaign<?, ?> c) {
        var list = new ArrayList<GameCampaign<T, I>>(getCampaigns());
        list.sort(Comparator.comparing(GameCampaign::getLastPlayed));
        Collections.reverse(list);
        return list.indexOf(c);
    }

    public Stream<GameCampaign<T, I>> campaignStream() {
        var list = new ArrayList<GameCampaign<T, I>>(getCampaigns());
        list.sort(Comparator.comparing(GameCampaign::getLastPlayed));
        Collections.reverse(list);
        return list.stream();
    }

    public ObservableSet<GameCampaign<T, I>> getCampaigns() {
        return campaigns;
    }

    public String getFileEnding() {
        return fileEnding;
    }

    public String getName() {
        return name;
    }
}

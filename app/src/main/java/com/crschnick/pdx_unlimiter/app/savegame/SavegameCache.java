package com.crschnick.pdx_unlimiter.app.savegame;

import com.crschnick.pdx_unlimiter.app.game.GameInstallation;
import com.crschnick.pdx_unlimiter.app.game.GameIntegration;
import com.crschnick.pdx_unlimiter.app.gui.ImageLoader;
import com.crschnick.pdx_unlimiter.app.installation.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.installation.IntegrityManager;
import com.crschnick.pdx_unlimiter.app.installation.PdxuInstallation;
import com.crschnick.pdx_unlimiter.app.installation.TaskExecutor;
import com.crschnick.pdx_unlimiter.app.util.ConfigHelper;
import com.crschnick.pdx_unlimiter.app.util.JsonHelper;
import com.crschnick.pdx_unlimiter.app.util.RakalyHelper;
import com.crschnick.pdx_unlimiter.core.data.GameDate;
import com.crschnick.pdx_unlimiter.core.data.GameDateType;
import com.crschnick.pdx_unlimiter.core.savegame.SavegameInfo;
import com.crschnick.pdx_unlimiter.core.savegame.SavegameParseException;
import com.crschnick.pdx_unlimiter.core.savegame.SavegameParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.scene.image.Image;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

    private Logger logger = LoggerFactory.getLogger(getClass());

    private Class<I> infoClass;
    private String fileEnding;
    private String name;
    private GameDateType dateType;
    private Path path;
    private SavegameParser<I> parser;
    private volatile ObservableSet<SavegameCollection<T, I>> collections = FXCollections.synchronizedObservableSet(
            FXCollections.observableSet(new HashSet<>()));

    public SavegameCache(
            String name,
            String fileEnding,
            GameDateType dateType,
            SavegameParser<I> parser,
            Class<I> infoClass) {
        this.name = name;
        this.parser = parser;
        this.fileEnding = fileEnding;
        this.dateType = dateType;
        this.path = PdxuInstallation.getInstance().getSavegamesLocation().resolve(name);
        this.infoClass = infoClass;
    }

    public static <T, I extends SavegameInfo<T>> SavegameCache<T, I> getForSavegame(SavegameEntry<T, I> e) {
        @SuppressWarnings("unchecked")
        Optional<SavegameCache<T, I>> sg = ALL.stream()
                .filter(i -> i.contains(e))
                .findFirst()
                .map(v -> (SavegameCache<T, I>) v);
        return sg.orElseThrow(IllegalArgumentException::new);
    }

    public static <T, I extends SavegameInfo<T>> SavegameCache<T, I> getForSavegame(SavegameCollection<T, I> col) {
        @SuppressWarnings("unchecked")
        Optional<SavegameCache<T, I>> sg = ALL.stream()
                .filter(i -> i.getCollections().contains(col))
                .findFirst()
                .map(v -> (SavegameCache<T, I>) v);
        return sg.orElseThrow(IllegalArgumentException::new);
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
            cache.loadData();
        }
    }

    public static void reset() {
        for (SavegameCache<?, ?> cache : ALL) {
            cache.saveData();
        }

        EU4 = null;
        HOI4 = null;
        STELLARIS = null;
        CK3 = null;
        ALL = Set.of();
    }

    private void loadData() {
        try {
            Files.createDirectories(getPath());
        } catch (IOException e) {
            ErrorHandler.handleTerminalException(e);
            return;
        }

        JsonNode node = null;
        if (Files.exists(getDataFile())) {
            node = ConfigHelper.readConfig(getDataFile());
        } else {
            return;
        }

        {
            JsonNode c = node.required("campaigns");
            for (int i = 0; i < c.size(); i++) {
                String name = c.get(i).required("name").textValue();
                GameDate date = dateType.fromString(c.get(i).required("date").textValue());
                UUID id = UUID.fromString(c.get(i).required("uuid").textValue());
                if (!Files.isDirectory(getPath().resolve(id.toString()))) {
                    continue;
                }

                Instant lastDate = Instant.parse(c.get(i).required("lastPlayed").textValue());
                Image image = ImageLoader.loadImage(getPath().resolve(id.toString()).resolve("campaign.png"));
                collections.add(new SavegameCampaign<T, I>(lastDate, name, id, date, image));
            }
        }

        {
            // Legacy support, can be missing
            JsonNode f = Optional.ofNullable(node.get("folders")).orElse(JsonNodeFactory.instance.arrayNode());
            for (int i = 0; i < f.size(); i++) {
                String name = f.get(i).required("name").textValue();
                UUID id = UUID.fromString(f.get(i).required("uuid").textValue());
                if (!Files.isDirectory(getPath().resolve(id.toString()))) {
                    continue;
                }

                Instant lastDate = Instant.parse(f.get(i).required("lastPlayed").textValue());
                collections.add(new SavegameFolder<>(lastDate, name, id));
            }
        }


        for (SavegameCollection<T, I> collection : collections) {
            try {
                String typeName = collection instanceof SavegameCampaign ? "campaign" : "folder";
                var colFile = getPath().resolve(
                        collection.getUuid().toString()).resolve(typeName + ".json");
                JsonNode campaignNode = JsonHelper.read(colFile);
                StreamSupport.stream(campaignNode.required("entries").spliterator(), false).forEach(entryNode -> {
                    UUID eId = UUID.fromString(entryNode.required("uuid").textValue());
                    String name = Optional.ofNullable(entryNode.get("name")).map(JsonNode::textValue).orElse(null);
                    GameDate date = dateType.fromString(entryNode.required("date").textValue());
                    String checksum = entryNode.required("checksum").textValue();
                    collection.add(new SavegameEntry<T, I>(name, eId, null, checksum, date));
                });
            } catch (Exception e) {
                ErrorHandler.handleException(e, "Could not load campaign config of " + collection.getName(), null);
            }
        }
    }

    private void saveData() {
        ObjectNode n = JsonNodeFactory.instance.objectNode();

        ArrayNode c = n.putArray("campaigns");
        getCollections().stream().filter(col -> col instanceof SavegameCampaign).forEach(col -> {
            SavegameCampaign<T, I> campaign = (SavegameCampaign<T, I>) col;
            ObjectNode campaignFileNode = JsonNodeFactory.instance.objectNode();
            ArrayNode entries = campaignFileNode.putArray("entries");
            campaign.getSavegames().stream()
                    .map(entry -> JsonNodeFactory.instance.objectNode()
                            .put("name", entry.getName())
                            .put("date", entry.getDate().toString())
                            .put("checksum", entry.getChecksum())
                            .put("uuid", entry.getUuid().toString()))
                    .forEach(entries::add);

            ConfigHelper.writeConfig(getPath()
                    .resolve(campaign.getUuid().toString()).resolve("campaign.json"), campaignFileNode);

            try {
                ImageLoader.writePng(campaign.getImage(), getPath()
                        .resolve(campaign.getUuid().toString()).resolve("campaign.png"));
            } catch (IOException e) {
                ErrorHandler.handleException(e);
            }

            ObjectNode campaignNode = JsonNodeFactory.instance.objectNode()
                    .put("name", campaign.getName())
                    .put("date", campaign.getDate().toString())
                    .put("lastPlayed", campaign.getLastPlayed().toString())
                    .put("uuid", campaign.getUuid().toString());
            c.add(campaignNode);
        });


        ArrayNode f = n.putArray("folders");
        getCollections().stream().filter(col -> col instanceof SavegameFolder).forEach(col -> {
            SavegameFolder<T, I> folder = (SavegameFolder<T, I>) col;
            ObjectNode folderFileNode = JsonNodeFactory.instance.objectNode();
            ArrayNode entries = folderFileNode.putArray("entries");
            folder.getSavegames().stream()
                    .map(entry -> JsonNodeFactory.instance.objectNode()
                            .put("name", entry.getName())
                            .put("date", entry.getDate().toString())
                            .put("checksum", entry.getChecksum())
                            .put("uuid", entry.getUuid().toString()))
                    .forEach(entries::add);

            ConfigHelper.writeConfig(getPath()
                    .resolve(folder.getUuid().toString()).resolve("folder.json"), folderFileNode);

            ObjectNode folderNode = JsonNodeFactory.instance.objectNode()
                    .put("name", folder.getName())
                    .put("lastPlayed", folder.getLastPlayed().toString())
                    .put("uuid", folder.getUuid().toString());
            f.add(folderNode);
        });

        ConfigHelper.writeConfig(getDataFile(), n);
    }

    public synchronized void delete(SavegameCollection<T, I> c) {
        if (!this.collections.contains(c)) {
            return;
        }

        Path campaignPath = path.resolve(c.getUuid().toString());
        try {
            FileUtils.deleteDirectory(campaignPath.toFile());
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (SavegameManagerState.get().globalSelectedCampaignProperty().get() == c) {
            SavegameManagerState.get().selectCollection(null);
        }

        this.collections.remove(c);
    }

    private synchronized Optional<SavegameFolder<T, I>> getOrCreateFolder(String name) {
        return this.collections.stream()
                .filter(f -> f instanceof SavegameFolder && f.getName().equals(name))
                .map(f -> (SavegameFolder<T, I>) f)
                .findAny()
                .or(() -> addNewFolder(name));
    }

    public synchronized Optional<SavegameFolder<T, I>> addNewFolder(String name) {
        var col = new SavegameFolder<T, I>(Instant.now(), name, UUID.randomUUID());
        try {
            Files.createDirectory(getPath().resolve(col.getUuid().toString()));
        } catch (IOException e) {
            ErrorHandler.handleException(e);
            return Optional.empty();
        }
        this.collections.add(col);
        return Optional.of(col);
    }

    public synchronized void addNewEntryToCampaign(UUID campainUuid, UUID entryUuid, String checksum, I info, String name) {
        SavegameEntry<T, I> e = new SavegameEntry<>(
                name != null ? name : getDefaultEntryName(info),
                entryUuid,
                info,
                checksum,
                info.getDate());
        if (this.getSavegameCollection(campainUuid).isEmpty()) {
            logger.debug("Adding new campaign " + getDefaultCampaignName(e));
            SavegameCampaign<T, I> newCampaign = new SavegameCampaign<>(
                    Instant.now(),
                    getDefaultCampaignName(e),
                    campainUuid,
                    e.getDate(),
                    GameIntegration.getForSavegameCache(this).getGuiFactory().tagImage(e.getInfo(), info.getTag()));
            this.collections.add(newCampaign);
        }

        SavegameCollection<T, I> c = this.getSavegameCollection(campainUuid).get();
        logger.debug("Adding new entry " + e.getName());
        c.add(e);

        SavegameManagerState.<T, I>get().selectEntry(e);
    }

    public synchronized void addNewEntryToCollection(SavegameCollection<T, I> col, UUID entryUuid, String checksum, I info, String name) {
        SavegameEntry<T, I> e = new SavegameEntry<>(
                name != null ? name : getDefaultEntryName(info),
                entryUuid,
                info,
                checksum,
                info.getDate());
        logger.debug("Adding new entry " + e.getName());
        col.getSavegames().add(e);

        SavegameManagerState.<T, I>get().selectEntry(e);
    }

    protected abstract String getDefaultEntryName(I info);

    protected abstract String getDefaultCampaignName(SavegameEntry<T, I> latest);

    public synchronized boolean contains(SavegameEntry<?, ?> e) {
        return collections.stream()
                .anyMatch(c -> c.getSavegames().stream().anyMatch(ce -> ce.getUuid().equals(e.getUuid())));
    }

    public synchronized SavegameCollection<T, I> getSavegameCollection(SavegameEntry<T, I> e) {
        var campaign = collections.stream()
                .filter(c -> c.getSavegames().stream().anyMatch(ce -> ce.getUuid().equals(e.getUuid())))
                .findAny();
        return campaign.orElseThrow(() -> new IllegalArgumentException(
                "Could not find savegame collection for entry " + e.getName()));
    }

    public void moveEntryAsync(
            SavegameCollection<T, I> to, SavegameEntry<T, I> entry) {
        TaskExecutor.getInstance().submitTask(() -> {
            moveEntry(to, entry);
        }, true, true);
    }

    private synchronized void moveEntry(
            SavegameCollection<T, I> to, SavegameEntry<T, I> entry) {
        var from = getSavegameCollection(entry);
        if (from == to) {
            return;
        }

        var srcDir = getPath(entry).toFile();
        try {
            FileUtils.copyDirectory(
                    srcDir,
                    getPath().resolve(to.getUuid().toString()).resolve(entry.getUuid().toString()).toFile());
        } catch (IOException e) {
            ErrorHandler.handleException(e);
            return;
        }

        from.getSavegames().remove(entry);
        to.getSavegames().add(entry);

        try {
            FileUtils.deleteDirectory(srcDir);
        } catch (IOException e) {
            ErrorHandler.handleException(e);
        }
        if (from.getSavegames().size() == 0) {
            delete(from);
        }

        saveData();
    }

    public void deleteAsync(SavegameEntry<T, I> e) {
        TaskExecutor.getInstance().submitTask(() -> {
            delete(e);
        }, false, false);
    }

    private synchronized void delete(SavegameEntry<T, I> e) {
        SavegameCollection<T, I> c = getSavegameCollection(e);
        if (!this.collections.contains(c) || !c.getSavegames().contains(e)) {
            return;
        }

        Path campaignPath = path.resolve(c.getUuid().toString());
        try {
            FileUtils.deleteDirectory(campaignPath.resolve(e.getUuid().toString()).toFile());
        } catch (IOException ex) {
            ErrorHandler.handleException(ex);
        }

        if (SavegameManagerState.get().globalSelectedEntryProperty().get() == e) {
            SavegameManagerState.get().selectEntry(null);
        }
        c.getSavegames().remove(e);
        if (c.getSavegames().size() == 0) {
            delete(c);
        }

        saveData();
    }

    public void loadEntryAsync(SavegameEntry<T, I> e) {
        TaskExecutor.getInstance().submitTask(() -> {
            loadEntry(e);
        }, false, false);
    }

    public void unloadCollectionAsync(SavegameCollection<T, I> col) {
        TaskExecutor.getInstance().submitTask(() -> {
            boolean loaded = col.getSavegames().stream().anyMatch(e -> e.getInfo() != null);
            if (!loaded) {
                return;
            }

            logger.debug("Unloading collection " + col.getName());
            for (var e : col.getSavegames()) {
                e.infoProperty().set(null);
            }
        }, false, false);
    }

    private synchronized void loadEntry(SavegameEntry<T, I> e) {
        if (e.infoProperty().isNotNull().get()) {
            return;
        }

        logger.debug("Starting to load entry " + getEntryName(e));
        if (e.infoProperty().isNotNull().get()) {
            logger.debug("Entry is already loaded");
            return;
        }

        if (Files.exists(getSavegameInfoFile(e))) {
            logger.debug("Info file already exists. Loading from file " + getSavegameInfoFile(e));
            try {
                e.infoProperty().set(JsonHelper.readObject(infoClass, getSavegameInfoFile(e)));
                return;
            } catch (Exception ex) {
                ErrorHandler.handleException(ex);
            }
        }

        var file = getSavegameFile(e);
        var status = parser.parse(file, RakalyHelper::meltSavegame);
        status.visit(new SavegameParser.StatusVisitor<I>() {
            @Override
            public void success(SavegameParser.Success<I> s) {
                logger.debug("Parsing was successful");
                e.infoProperty().set(s.info);

                try {
                    // Clear old info files
                    Files.list(getPath(e)).filter(p -> !p.equals(getSavegameFile(e))).forEach(p -> {
                        try {
                            logger.debug("Deleting old info file " + p.toString());
                            Files.delete(p);
                        } catch (IOException ioException) {
                            ErrorHandler.handleException(ioException);
                        }
                    });

                    logger.debug("Writing new info to file " + getSavegameInfoFile(e));
                    JsonHelper.writeObject(s.info, getSavegameInfoFile(e));
                } catch (Exception ex) {
                    ErrorHandler.handleException(ex);
                }
            }

            @Override
            public void error(SavegameParser.Error e) {
                ErrorHandler.handleException(e.error, null, file);
            }

            @Override
            public void invalid(SavegameParser.Invalid iv) {
                ErrorHandler.handleException(new SavegameParseException(iv.message), null, file);
            }
        });
    }

    public synchronized Path getSavegameFile(SavegameEntry<T, I> e) {
        return getPath(e).resolve("savegame." + fileEnding);
    }

    public synchronized Path getSavegameInfoFile(SavegameEntry<T, I> e) {
        return getPath(e).resolve(getInfoFileName());
    }

    public synchronized Path getPath(SavegameEntry<T, I> e) {
        Path campaignPath = path.resolve(getSavegameCollection(e).getUuid().toString());
        return campaignPath.resolve(e.getUuid().toString());
    }

    public synchronized Optional<SavegameCollection<T, I>> getSavegameCollection(UUID uuid) {
        for (SavegameCollection<T, I> c : collections) {
            if (c.getUuid().equals(uuid)) {
                return Optional.of(c);
            }
        }
        return Optional.empty();
    }

    public synchronized String getFileName(SavegameEntry<T, I> e) {
        var colName = getSavegameCollection(e).getName().replaceAll("[\\\\/:*?\"<>|]", "_");
        var sgName = e.getName().replaceAll("[\\\\/:*?\"<>|]", "_");
        return colName + " (" + sgName + ")." + fileEnding;
    }

    public synchronized void exportSavegame(SavegameEntry<T, I> e, Path destPath) throws IOException {
        Path srcPath = getPath(e).resolve("savegame." + fileEnding);
        FileUtils.forceMkdirParent(destPath.toFile());
        FileUtils.copyFile(srcPath.toFile(), destPath.toFile(), false);
        destPath.toFile().setLastModified(Instant.now().toEpochMilli());
    }

    synchronized SavegameParser.Status importSavegame(Path file, String name, boolean checkDuplicate, SavegameCollection<T, I> folder) {
        var status = importSavegameData(file, name, checkDuplicate, folder);
        saveData();
        return status;
    }

    private String getSaveFileName() {
        return "savegame." + fileEnding;
    }

    private String getInfoFileName() {
        return "info_" + IntegrityManager.getInstance().getCoreChecksum() + ".json";
    }

    public synchronized void meltSavegame(SavegameEntry<T, I> e) {
        logger.debug("Melting savegame");
        Path meltedFile;
        try {
            meltedFile = RakalyHelper.meltSavegame(getSavegameFile(e));
        } catch (Exception ex) {
            ErrorHandler.handleException(ex);
            return;
        }
        var folder = getOrCreateFolder("Melted savegames");
        folder.ifPresent(f -> {
            importSavegame(meltedFile, null, true, f);
        });
    }

    public synchronized void reloadSavegameAsync(SavegameEntry<T, I> e) {
        TaskExecutor.getInstance().submitTask(() -> {
            logger.debug("Reloading savegame");
            e.infoProperty().set(null);
            var status = parser.parse(getSavegameFile(e), RakalyHelper::meltSavegame);
            status.visit(new SavegameParser.StatusVisitor<I>() {
                @Override
                public void success(SavegameParser.Success<I> s) {
                    logger.debug("Reloading was successful");
                    try {
                        JsonHelper.writeObject(s.info, getSavegameInfoFile(e));
                        e.infoProperty().set(s.info);
                    } catch (IOException ioException) {
                        ErrorHandler.handleException(ioException);
                    }
                }

                @Override
                public void error(SavegameParser.Error e) {
                    ErrorHandler.handleException(e.error);
                }

                @Override
                public void invalid(SavegameParser.Invalid iv) {
                    ErrorHandler.handleException(new SavegameParseException(iv.message));
                }
            });
        }, false, true);
    }

    private SavegameParser.Status importSavegameData(Path file, String name, boolean checkDuplicate, SavegameCollection<T, I> folder) {
        logger.debug("Parsing file " + file.toString());
        var status = parser.parse(file, RakalyHelper::meltSavegame);
        status.visit(new SavegameParser.StatusVisitor<I>() {
            @Override
            public void success(SavegameParser.Success<I> s) {
                logger.debug("Parsing was successful");
                logger.debug("Checksum is " + s.checksum);
                if (checkDuplicate) {
                    var exists = getCollections().stream().flatMap(SavegameCollection::entryStream)
                            .filter(ch -> ch.getChecksum().equals(s.checksum))
                            .findAny();
                    if (exists.isPresent()) {
                        logger.debug("Entry " + exists.get().getName() + " with checksum already in storage");
                        loadEntry(exists.get());
                        SavegameManagerState.<T, I>get().selectEntry(exists.get());
                        return;
                    } else {
                        logger.debug("No entry with checksum found");
                    }
                }

                UUID collectionUuid;
                if (folder == null) {
                    collectionUuid = s.info.getCampaignHeuristic();
                    logger.debug("Campaign UUID is " + collectionUuid.toString());
                } else {
                    collectionUuid = folder.getUuid();
                    logger.debug("Folder UUID is " + collectionUuid.toString());
                }
                UUID saveUuid = UUID.randomUUID();
                logger.debug("Generated savegame UUID " + saveUuid.toString());

                Path entryPath = getPath().resolve(collectionUuid.toString()).resolve(saveUuid.toString());
                try {
                    FileUtils.forceMkdir(entryPath.toFile());
                    FileUtils.copyFile(file.toFile(), entryPath.resolve(getSaveFileName()).toFile());
                    JsonHelper.writeObject(s.info, entryPath.resolve(getInfoFileName()));
                } catch (Exception e) {
                    ErrorHandler.handleException(e);
                    return;
                }

                if (folder == null) {
                    addNewEntryToCampaign(collectionUuid, saveUuid, s.checksum, s.info, name);
                } else {
                    addNewEntryToCollection(folder, saveUuid, s.checksum, s.info, name);
                }
            }

            @Override
            public void error(SavegameParser.Error e) {
                logger.error("An error occured during parsing: " + e.error.getMessage());
            }

            @Override
            public void invalid(SavegameParser.Invalid iv) {
                logger.error("Savegame is invalid: " + iv.message);
            }
        });
        return status;
    }

    public String getEntryName(SavegameEntry<T, I> e) {
        String cn = getSavegameCollection(e).getName();
        String en = e.getName();
        return cn + " (" + en + ")";
    }

    public Path getPath() {
        return path;
    }

    public Path getDataFile() {
        return getPath().resolve("campaigns.json");
    }

    public ObservableSet<SavegameCollection<T, I>> getCollections() {
        return collections;
    }

    public String getFileEnding() {
        return fileEnding;
    }

    public String getName() {
        return name;
    }

    public SavegameParser<I> getParser() {
        return parser;
    }
}

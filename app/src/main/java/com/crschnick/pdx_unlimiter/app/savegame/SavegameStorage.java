package com.crschnick.pdx_unlimiter.app.savegame;

import com.crschnick.pdx_unlimiter.app.core.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.core.PdxuInstallation;
import com.crschnick.pdx_unlimiter.app.gui.game.ImageLoader;
import com.crschnick.pdx_unlimiter.app.installation.GameInstallation;
import com.crschnick.pdx_unlimiter.app.installation.GameIntegration;
import com.crschnick.pdx_unlimiter.app.savegame.game.Ck3SavegameStorage;
import com.crschnick.pdx_unlimiter.app.savegame.game.Eu4SavegameStorage;
import com.crschnick.pdx_unlimiter.app.savegame.game.Hoi4SavegameStorage;
import com.crschnick.pdx_unlimiter.app.savegame.game.StellarisSavegameStorage;
import com.crschnick.pdx_unlimiter.app.util.ConfigHelper;
import com.crschnick.pdx_unlimiter.app.util.JsonHelper;
import com.crschnick.pdx_unlimiter.app.util.RakalyHelper;
import com.crschnick.pdx_unlimiter.core.info.GameDate;
import com.crschnick.pdx_unlimiter.core.info.GameDateType;
import com.crschnick.pdx_unlimiter.core.info.SavegameInfo;
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

public abstract class SavegameStorage<
        T,
        I extends SavegameInfo<T>> {

    public static Eu4SavegameStorage EU4;
    public static Hoi4SavegameStorage HOI4;
    public static StellarisSavegameStorage STELLARIS;
    public static Ck3SavegameStorage CK3;
    public static Set<SavegameStorage<?, ?>> ALL;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Class<I> infoClass;
    private final String fileEnding;
    private final String name;
    private final GameDateType dateType;
    private final Path path;
    private final SavegameParser parser;
    private final String infoChecksum;
    private final ObservableSet<SavegameCollection<T, I>> collections = FXCollections.observableSet(new HashSet<>());

    public SavegameStorage(
            String name,
            String fileEnding,
            GameDateType dateType,
            SavegameParser parser,
            Class<I> infoClass,
            String infoChecksum) {
        this.name = name;
        this.parser = parser;
        this.fileEnding = fileEnding;
        this.dateType = dateType;
        this.path = PdxuInstallation.getInstance().getSavegamesLocation().resolve(name);
        this.infoClass = infoClass;
        this.infoChecksum = infoChecksum;
    }

    public static void init() {
        if (GameInstallation.EU4 != null) {
            EU4 = new Eu4SavegameStorage();
        }
        if (GameInstallation.HOI4 != null) {
            HOI4 = new Hoi4SavegameStorage();
        }
        if (GameInstallation.STELLARIS != null) {
            STELLARIS = new StellarisSavegameStorage();
        }
        if (GameInstallation.CK3 != null) {
            CK3 = new Ck3SavegameStorage();
        }
        ALL = Stream.of(EU4, HOI4, STELLARIS, CK3)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        for (SavegameStorage<?, ?> cache : ALL) {
            cache.loadData();
        }
    }

    public static void reset() {
        for (SavegameStorage<?, ?> cache : ALL) {
            cache.saveData();
        }

        EU4 = null;
        HOI4 = null;
        STELLARIS = null;
        CK3 = null;
        ALL = Set.of();
    }

    private synchronized void loadData() {
        try {
            Files.createDirectories(getPath());
        } catch (IOException e) {
            ErrorHandler.handleTerminalException(e);
            return;
        }

        JsonNode node;
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
                collections.add(new SavegameCampaign<>(lastDate, name, id, date, image));
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
                    collection.add(new SavegameEntry<>(name, eId, null, checksum, date));
                });
            } catch (Exception e) {
                ErrorHandler.handleException(e, "Could not load campaign config of " + collection.getName(), null);
            }
        }
    }

    private synchronized void saveData() {
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
                            .put("checksum", entry.getContentChecksum())
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
                            .put("checksum", entry.getContentChecksum())
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

    synchronized Optional<SavegameFolder<T, I>> getOrCreateFolder(String name) {
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
                // Set info to null, since we don't want to store it
                // directly after importing. It can be loaded later
                null,
                checksum,
                info.getDate());
        if (this.getSavegameCollection(campainUuid).isEmpty()) {
            logger.debug("Adding new campaign " + getDefaultCampaignName(info));
            SavegameCampaign<T, I> newCampaign = new SavegameCampaign<>(
                    Instant.now(),
                    getDefaultCampaignName(info),
                    campainUuid,
                    e.getDate(),
                    GameIntegration.getForSavegameStorage(this).getGuiFactory().tagImage(info, info.getTag()));
            this.collections.add(newCampaign);
        }

        SavegameCollection<T, I> c = this.getSavegameCollection(campainUuid).get();
        logger.debug("Adding new entry " + e.getName());
        c.add(e);
        c.onSavegamesChange();
    }

    public synchronized void addNewEntryToCollection(SavegameCollection<T, I> col, UUID entryUuid, String checksum, I info, String name) {
        SavegameEntry<T, I> e = new SavegameEntry<>(
                name != null ? name : getDefaultEntryName(info),
                entryUuid,
                null,
                checksum,
                info.getDate());
        logger.debug("Adding new entry " + e.getName());
        col.getSavegames().add(e);
        col.onSavegamesChange();
    }

    protected abstract String getDefaultEntryName(I info);

    protected abstract String getDefaultCampaignName(I info);

    public synchronized boolean contains(SavegameEntry<?, ?> e) {
        return collections.stream()
                .anyMatch(c -> c.getSavegames().stream().anyMatch(ce -> ce.getUuid().equals(e.getUuid())));
    }

    public synchronized SavegameCollection<T, I> getSavegameCollection(SavegameEntry<?, ?> e) {
        var campaign = collections.stream()
                .filter(c -> c.getSavegames().stream().anyMatch(ce -> ce.getUuid().equals(e.getUuid())))
                .findAny();
        return campaign.orElseThrow(() -> new IllegalArgumentException(
                "Could not find savegame collection for entry " + e.getName()));
    }

    synchronized void moveEntry(
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
        from.onSavegamesChange();
        to.getSavegames().add(entry);
        to.onSavegamesChange();

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

    synchronized void delete(SavegameCollection<T, I> c) {
        if (!this.collections.contains(c)) {
            return;
        }

        Path campaignPath = path.resolve(c.getUuid().toString());
        try {
            FileUtils.deleteDirectory(campaignPath.toFile());
        } catch (IOException e) {
            ErrorHandler.handleException(e);
        }

        this.collections.remove(c);
    }


    synchronized void delete(SavegameEntry<T, I> e) {
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

        c.getSavegames().remove(e);
        c.onSavegamesChange();
        if (c.getSavegames().size() == 0) {
            delete(c);
        }

        saveData();
    }

    public synchronized void loadEntry(SavegameEntry<T, I> e) {
        if (e.infoProperty().isNotNull().get()) {
            return;
        }

        if (Files.exists(getSavegameInfoFile(e))) {
            logger.debug("Info file already exists. Loading from file " + getSavegameInfoFile(e));
            try {
                e.infoProperty().set(JsonHelper.readObject(infoClass, getSavegameInfoFile(e)));
                getSavegameCollection(e).onSavegameLoad(e);
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
                getSavegameCollection(e).onSavegameLoad(e);

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

    public synchronized void copySavegameTo(SavegameEntry<T, I> e, Path destPath) throws IOException {
        Path srcPath = getSavegameFile(e);
        FileUtils.forceMkdirParent(destPath.toFile());
        FileUtils.copyFile(srcPath.toFile(), destPath.toFile(), false);
        destPath.toFile().setLastModified(Instant.now().toEpochMilli());
    }

    protected SavegameParser.Status importSavegame(Path file, String name, boolean checkDuplicate, SavegameCollection<T, I> folder) {
        var status = importSavegameData(file, name, checkDuplicate, folder);
        saveData();
        return status;
    }

    private String getSaveFileName() {
        return "savegame." + fileEnding;
    }

    private String getInfoFileName() {
        return "info_" + infoChecksum + ".json";
    }

    public synchronized void invalidateSavegameInfo(SavegameEntry<T, I> e) {
        if (Files.exists(getSavegameInfoFile(e))) {
            logger.debug("Invalidating " + getSavegameInfoFile(e));
            try {
                Files.delete(getSavegameInfoFile(e));
                return;
            } catch (Exception ex) {
                ErrorHandler.handleException(ex);
            }
        }
    }

    void reloadSavegameAsync(SavegameEntry<T, I> e) {
        logger.debug("Reloading savegame");
        e.infoProperty().set(null);
        var status = parser.parse(getSavegameFile(e), RakalyHelper::meltSavegame);
        status.visit(new SavegameParser.StatusVisitor<I>() {
            @Override
            public void success(SavegameParser.Success<I> s) {
                logger.debug("Reloading was successful");
                synchronized (SavegameStorage.this) {
                    try {
                        JsonHelper.writeObject(s.info, getSavegameInfoFile(e));
                        e.infoProperty().set(s.info);
                        getSavegameCollection(e).onSavegameLoad(e);
                    } catch (IOException ioException) {
                        ErrorHandler.handleException(ioException);
                    }
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
    }

    private SavegameParser.Status importSavegameData(Path file, String name, boolean checkDuplicate, SavegameCollection<T, I> col) {
        logger.debug("Parsing file " + file.toString());
        var status = parser.parse(file, RakalyHelper::meltSavegame);
        status.visit(new SavegameParser.StatusVisitor<I>() {
            @Override
            public void success(SavegameParser.Success<I> s) {
                logger.debug("Parsing was successful");
                logger.debug("Checksum is " + s.checksum);
                if (checkDuplicate) {
                    var exists = getSavegameForChecksum(s.checksum);
                    if (exists.isPresent()) {
                        logger.debug("Entry " + exists.get().getName() + " with checksum already in storage");
                        loadEntry(exists.get());
                        return;
                    } else {
                        logger.debug("No entry with checksum found");
                    }
                }

                UUID collectionUuid;
                if (col == null) {
                    collectionUuid = s.info.getCampaignHeuristic();
                    logger.debug("Campaign UUID is " + collectionUuid.toString());
                } else {
                    collectionUuid = col.getUuid();
                    logger.debug("Folder UUID is " + collectionUuid.toString());
                }
                UUID saveUuid = UUID.randomUUID();
                logger.debug("Generated savegame UUID " + saveUuid.toString());

                synchronized (this) {
                    Path entryPath = getPath().resolve(collectionUuid.toString()).resolve(saveUuid.toString());
                    try {
                        FileUtils.forceMkdir(entryPath.toFile());
                        FileUtils.copyFile(file.toFile(), entryPath.resolve(getSaveFileName()).toFile());
                        JsonHelper.writeObject(s.info, entryPath.resolve(getInfoFileName()));
                    } catch (Exception e) {
                        ErrorHandler.handleException(e);
                        return;
                    }

                    if (col == null) {
                        addNewEntryToCampaign(collectionUuid, saveUuid, s.checksum, s.info, name);
                    } else {
                        addNewEntryToCollection(col, saveUuid, s.checksum, s.info, name);
                    }
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

    public synchronized Optional<SavegameEntry<T,I>> getSavegameForChecksum(String cs) {
        return getCollections().stream().flatMap(SavegameCollection::entryStream)
                .filter(ch -> ch.getContentChecksum().equals(cs))
                .findAny();
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

    public String getName() {
        return name;
    }

    public SavegameParser getParser() {
        return parser;
    }
}

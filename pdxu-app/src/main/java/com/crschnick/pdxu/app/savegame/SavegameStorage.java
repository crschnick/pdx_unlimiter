package com.crschnick.pdxu.app.savegame;

import com.crschnick.pdxu.app.core.ErrorHandler;
import com.crschnick.pdxu.app.core.IntegrityManager;
import com.crschnick.pdxu.app.core.settings.Settings;
import com.crschnick.pdxu.app.gui.game.GameGuiFactory;
import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.app.lang.GameLocalisation;
import com.crschnick.pdxu.app.lang.LanguageManager;
import com.crschnick.pdxu.app.lang.PdxuI18n;
import com.crschnick.pdxu.app.util.ConfigHelper;
import com.crschnick.pdxu.app.util.ImageHelper;
import com.crschnick.pdxu.app.util.JsonHelper;
import com.crschnick.pdxu.app.util.integration.RakalyHelper;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.savegame.SavegameContent;
import com.crschnick.pdxu.io.savegame.SavegameParseResult;
import com.crschnick.pdxu.io.savegame.SavegameType;
import com.crschnick.pdxu.model.GameDate;
import com.crschnick.pdxu.model.GameDateType;
import com.crschnick.pdxu.model.SavegameInfo;
import com.crschnick.pdxu.model.SavegameInfoException;
import com.crschnick.pdxu.model.ck2.Ck2SavegameInfo;
import com.crschnick.pdxu.model.ck3.Ck3SavegameInfo;
import com.crschnick.pdxu.model.eu4.Eu4SavegameInfo;
import com.crschnick.pdxu.model.hoi4.Hoi4SavegameInfo;
import com.crschnick.pdxu.model.stellaris.StellarisSavegameInfo;
import com.crschnick.pdxu.model.vic2.Vic2SavegameInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.scene.image.Image;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.function.FailableBiFunction;
import org.apache.commons.lang3.function.FailableConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public abstract class SavegameStorage<
        T,
        I extends SavegameInfo<T>> {


    public static final BidiMap<Game, SavegameStorage<?, ?>> ALL = new DualHashBidiMap<>();
    private final Logger logger;
    private final Class<I> infoClass;
    private final FailableBiFunction<SavegameContent, Boolean, I, SavegameInfoException> infoFactory;
    private final String name;
    private final GameDateType dateType;
    private final Path path;
    private final SavegameType type;
    private final ObservableSet<SavegameCollection<T, I>> collections = FXCollections.observableSet(new HashSet<>());

    public SavegameStorage(
            FailableBiFunction<SavegameContent, Boolean, I, SavegameInfoException> infoFactory,
            String name,
            GameDateType dateType,
            SavegameType type,
            Class<I> infoClass) {
        this.infoFactory = infoFactory;
        this.name = name;
        this.type = type;
        this.dateType = dateType;
        this.path = Settings.getInstance().storageDirectory.getValue().resolve(name);
        this.infoClass = infoClass;
        this.logger = LoggerFactory.getLogger("SavegameStorage (" + getName() + ")");
    }

    @SuppressWarnings("unchecked")
    public static <T, I extends SavegameInfo<T>> SavegameStorage<T, I> get(Game g) {
        return (SavegameStorage<T, I>) ALL.get(g);
    }

    @SuppressWarnings("unchecked")
    public static <T, I extends SavegameInfo<T>> SavegameStorage<T, I> get(SavegameType type) {
        var found = ALL.entrySet().stream()
                .filter(entry -> entry.getValue().type.equals(type))
                .findAny();
        return (SavegameStorage<T, I>) found.map(e -> e.getValue()).orElse(null);
    }

    public static void init() throws Exception {
        ALL.put(Game.EU4, new SavegameStorage<>(
                (node, melted) -> Eu4SavegameInfo.fromSavegame(melted, node),
                "eu4",
                GameDateType.EU4,
                SavegameType.EU4,
                Eu4SavegameInfo.class) {
            @Override
            protected String getDefaultCampaignName(Eu4SavegameInfo info) {
                return GameLocalisation.getLocalisedValue(info.getTag().getTag(), info);
            }
        });
        ALL.put(Game.HOI4, new SavegameStorage<>(
                (node, melted) -> Hoi4SavegameInfo.fromSavegame(melted, node),
                "hoi4",
                GameDateType.HOI4,
                SavegameType.HOI4,
                Hoi4SavegameInfo.class
        ) {
            @Override
            protected String getDefaultCampaignName(Hoi4SavegameInfo info) {
                return "Unknown";
            }
        });
        ALL.put(Game.CK3, new SavegameStorage<>(
                (node, melted) -> Ck3SavegameInfo.fromSavegame(melted, node),
                "ck3",
                GameDateType.CK3,
                SavegameType.CK3,
                Ck3SavegameInfo.class
        ) {
            @Override
            protected String getDefaultCampaignName(Ck3SavegameInfo info) {
                if (info.isObserver()) {
                    return "Observer";
                }

                if (!info.hasOnePlayerTag()) {
                    return "Unknown";
                }

                return info.getTag().getName();
            }
        });
        ALL.put(Game.STELLARIS, new SavegameStorage<>(
                (node, melted) -> StellarisSavegameInfo.fromSavegame(node),
                "stellaris",
                GameDateType.STELLARIS,
                SavegameType.STELLARIS,
                StellarisSavegameInfo.class
        ) {
            @Override
            protected String getDefaultCampaignName(StellarisSavegameInfo info) {
                return info.getTag().getName();
            }
        });
        ALL.put(Game.CK2, new SavegameStorage<>(
                (node, melted) -> new Ck2SavegameInfo(node),
                "ck2",
                GameDateType.CK2,
                SavegameType.CK2,
                Ck2SavegameInfo.class
        ) {
            @Override
            protected String getDefaultCampaignName(Ck2SavegameInfo info) {
                return info.getTag().getRulerName();
            }
        });
        ALL.put(Game.VIC2, new SavegameStorage<>(
                (node, melted) -> new Vic2SavegameInfo(node),
                "vic2",
                GameDateType.VIC2,
                SavegameType.VIC2,
                Vic2SavegameInfo.class
        ) {
            @Override
            protected String getDefaultCampaignName(Vic2SavegameInfo info) {
                return "Unknown";
            }
        });
        for (SavegameStorage<?, ?> s : ALL.values()) {
            s.loadData();
        }
    }

    public static void reset() {
        for (SavegameStorage<?, ?> s : ALL.values()) {
            s.saveData();
        }
        ALL.clear();
    }

    private synchronized void loadData() throws Exception {
        Files.createDirectories(getSavegameDataDirectory());

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
                if (!Files.isDirectory(getSavegameDataDirectory().resolve(id.toString()))) {
                    continue;
                }

                Instant lastDate = Instant.parse(c.get(i).required("lastPlayed").textValue());
                Image image = ImageHelper.loadImage(
                        getSavegameDataDirectory().resolve(id.toString()).resolve("campaign.png"));
                collections.add(new SavegameCampaign<>(lastDate, name, id, date, image));
            }
        }

        {
            // Legacy support, can be missing
            JsonNode f = Optional.ofNullable(node.get("folders")).orElse(JsonNodeFactory.instance.arrayNode());
            for (int i = 0; i < f.size(); i++) {
                String name = f.get(i).required("name").textValue();
                UUID id = UUID.fromString(f.get(i).required("uuid").textValue());
                if (!Files.isDirectory(getSavegameDataDirectory().resolve(id.toString()))) {
                    continue;
                }

                Instant lastDate = Instant.parse(f.get(i).required("lastPlayed").textValue());
                collections.add(new SavegameFolder<>(lastDate, name, id));
            }
        }


        for (SavegameCollection<T, I> collection : collections) {
            String typeName = collection instanceof SavegameCampaign ? "campaign" : "folder";
            var colFile = getSavegameDataDirectory().resolve(
                    collection.getUuid().toString()).resolve(typeName + ".json");

            // Campaign file might not exist even though the directory exists. Do not fail in this case
            if (!Files.exists(colFile)) {
                continue;
            }

            JsonNode campaignNode = JsonHelper.read(colFile);
            StreamSupport.stream(campaignNode.required("entries").spliterator(), false).forEach(entryNode -> {
                UUID eId = UUID.fromString(entryNode.required("uuid").textValue());
                String name = Optional.ofNullable(entryNode.get("name")).map(JsonNode::textValue).orElse(null);
                GameDate date = dateType.fromString(entryNode.required("date").textValue());
                String checksum = entryNode.required("checksum").textValue();
                SavegameNotes notes = SavegameNotes.fromNode(entryNode.get("notes"));
                List<String> sourceFileChecksums = Optional.ofNullable(entryNode.get("sourceFileChecksums"))
                        .map(n -> StreamSupport.stream(n.spliterator(), false)
                                .map(JsonNode::textValue)
                                .collect(Collectors.toList()))
                        .orElse(List.of());
                collection.add(new SavegameEntry<>(name, eId, checksum, date, notes, sourceFileChecksums));
            });
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
                            .put("uuid", entry.getUuid().toString())
                            .<ObjectNode>set("sourceFileChecksums", JsonNodeFactory.instance.arrayNode().addAll(
                                    entry.getSourceFileChecksums().stream()
                                            .map(s -> new TextNode(s))
                                            .collect(Collectors.toList())))
                            .<ObjectNode>set("notes", SavegameNotes.toNode(entry.getNotes())))
                    .forEach(entries::add);

            ConfigHelper.writeConfig(getSavegameDataDirectory()
                    .resolve(campaign.getUuid().toString()).resolve("campaign.json"), campaignFileNode);

            var imgFile = getSavegameDataDirectory()
                    .resolve(campaign.getUuid().toString()).resolve("campaign.png");
            try {
                ImageHelper.writePng(campaign.getImage(), imgFile);
            } catch (IOException e) {
                logger.error("Couldn't write image " + imgFile, e);
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
                            .put("uuid", entry.getUuid().toString())
                            .<ObjectNode>set("sourceFileChecksums", JsonNodeFactory.instance.arrayNode().addAll(
                                    entry.getSourceFileChecksums().stream()
                                            .map(s -> new TextNode(s))
                                            .collect(Collectors.toList())))
                            .<ObjectNode>set("notes", SavegameNotes.toNode(entry.getNotes())))
                    .forEach(entries::add);

            ConfigHelper.writeConfig(getSavegameDataDirectory()
                    .resolve(folder.getUuid().toString()).resolve("folder.json"), folderFileNode);

            ObjectNode folderNode = JsonNodeFactory.instance.objectNode()
                    .put("name", folder.getName())
                    .put("lastPlayed", folder.getLastPlayed().toString())
                    .put("uuid", folder.getUuid().toString());
            f.add(folderNode);
        });

        ConfigHelper.writeConfig(getDataFile(), n);
    }

    public synchronized void addNewEntryToCampaign(
            UUID campainUuid,
            UUID entryUuid,
            String checksum,
            I info,
            String name,
            String sourceFileChecksum,
            String defaultCampaignName) {
        SavegameEntry<T, I> e = new SavegameEntry<>(
                name != null ? name : getDefaultEntryName(info),
                entryUuid,
                checksum,
                info.getDate(),
                SavegameNotes.empty(),
                sourceFileChecksum != null ? List.of(sourceFileChecksum) : List.of());
        if (this.getSavegameCollection(campainUuid).isEmpty()) {
            logger.debug("Adding new campaign " + getDefaultCampaignName(info));
            var img = GameGuiFactory.<T, I>get(ALL.inverseBidiMap().get(this))
                    .tagImage(info, info.getTag());
            SavegameCampaign<T, I> newCampaign = new SavegameCampaign<>(
                    Instant.now(),
                    defaultCampaignName != null ? defaultCampaignName : getDefaultCampaignName(info),
                    campainUuid,
                    e.getDate(),
                    img);
            this.collections.add(newCampaign);
        }

        SavegameCollection<T, I> c = this.getSavegameCollection(campainUuid).get();
        logger.debug("Adding new entry " + e.getName());
        c.add(e);
        c.onSavegamesChange();
    }

    private String getDefaultEntryName(I info) {
        return info.getDate().toDisplayString(LanguageManager.getInstance().getActiveLanguage().getLocale());
    }

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

        var srcDir = getSavegameDataDirectory(entry).toFile();
        try {
            FileUtils.copyDirectory(
                    srcDir,
                    getSavegameDataDirectory().resolve(to.getUuid().toString()).resolve(entry.getUuid().toString()).toFile());
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
            // Don't show the user this error. It sometimes happens when the file is
            // used by another process or even an antivirus program
            logger.error("Could not delete collection " + c.getName(), c);
        }

        this.collections.remove(c);

        saveData();
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
            // Don't show the user this error. It sometimes happens when the file is
            // used by another process or even an antivirus program
            logger.error("Could not delete entry " + e.getName(), ex);
        }

        c.getSavegames().remove(e);
        c.onSavegamesChange();
        if (c.getSavegames().size() == 0) {
            delete(c);
        }

        saveData();
    }

    public synchronized void loadEntry(SavegameEntry<T, I> e) {
        if (!e.canLoad()) {
            return;
        }


        var file = getSavegameFile(e);
        if (!Files.exists(file)) {
            e.fail();
            return;
        }

        if (Files.exists(getSavegameInfoFile(e))) {
            logger.debug("Info file already exists. Loading from file " + getSavegameInfoFile(e));
            try {
                e.startLoading();
                e.load(JsonHelper.readObject(infoClass, getSavegameInfoFile(e)));
                getSavegameCollection(e).onSavegameLoad(e);
                return;
            } catch (Exception ex) {
                ErrorHandler.handleException(ex);
            }
        }

        e.startLoading();

        SavegameParseResult result;
        boolean melted;
        try {
            var bytes = Files.readAllBytes(file);
            if (type.isBinary(bytes)) {
                bytes = RakalyHelper.toEquivalentPlaintext(file);
                melted = true;
            } else {
                melted = false;
            }
            var struc = type.determineStructure(bytes);
            result = struc.parse(bytes);
        } catch (Exception ex) {
            ErrorHandler.handleException(ex);
            e.fail();
            return;
        }

        result.visit(new SavegameParseResult.Visitor() {
            @Override
            public void success(SavegameParseResult.Success s) {
                try {
                    logger.debug("Parsing was successful. Loading info ...");
                    I info = infoFactory.apply(s.content, melted);
                    e.load(info);
                    getSavegameCollection(e).onSavegameLoad(e);


                    // Clear old info files
                    Files.list(getSavegameDataDirectory(e)).filter(p -> !p.equals(getSavegameFile(e))).forEach(p -> {
                        try {
                            logger.debug("Deleting old info file " + p.toString());
                            Files.delete(p);
                        } catch (IOException ioException) {
                            ErrorHandler.handleException(ioException);
                        }
                    });

                    logger.debug("Writing new info to file " + getSavegameInfoFile(e));
                    JsonHelper.writeObject(info, getSavegameInfoFile(e));
                } catch (Exception ex) {
                    ErrorHandler.handleException(ex);
                    e.fail();
                }
            }

            @Override
            public void error(SavegameParseResult.Error er) {
                e.fail();
                ErrorHandler.handleException(er.error, null, file);
            }

            @Override
            public void invalid(SavegameParseResult.Invalid iv) {
                e.fail();
                ErrorHandler.handleException(new IllegalArgumentException(iv.message), null, file);
            }
        });
    }

    public synchronized Optional<SavegameEntry<T,I>> getEntryForStorageSavegameFile(Path file) {
        return getCollections().stream().flatMap(SavegameCollection::entryStream)
                .filter(ch -> getSavegameFile(ch).equals(file))
                .findAny();
    }

    public synchronized Path getSavegameFile(SavegameEntry<?, ?> e) {
        return getSavegameDataDirectory(e).resolve("savegame." + type.getFileEnding());
    }

    public synchronized Path getSavegameInfoFile(SavegameEntry<T, I> e) {
        return getSavegameDataDirectory(e).resolve(getInfoFileName());
    }

    public synchronized Path getSavegameDataDirectory(SavegameEntry<?, ?> e) {
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

    public synchronized Optional<UUID> getCustomCampaignId(SavegameEntry<?, ?> e) throws Exception {
        if (!e.isLoaded()) {
            throw new IllegalStateException("Savegame info not available");
        }

        var savegameCampaignId = e.getInfo().getCampaignHeuristic();
        var storageCampaignId = getSavegameCollection(e).getUuid();
        return savegameCampaignId.equals(storageCampaignId) ? Optional.empty() : Optional.of(storageCampaignId);
    }

    public synchronized Path getValidOutputFileName(SavegameEntry<?, ?> e, boolean includeEntryName, String suffix) {
        var name = getSavegameCollection(e).getName() + (includeEntryName ?
                " (" + e.getName() + ")" : "") + (suffix != null ? suffix : "");
        var comp = SavegameContext.getForSavegame(e).getInstallType().getCompatibleSavegameName(name);

        // Try to return valid file name
        if (comp.length() > 0) {
            try {
                return Path.of(comp + "." + type.getFileEnding());
            } catch (InvalidPathException ignored) {
            }
        }

        // Fallback
        return Path.of("invalid-name" + (suffix != null ? suffix : "") + "." + type.getFileEnding());
    }

    public synchronized void copySavegameTo(SavegameEntry<T, I> e, Path destPath) throws IOException {
        Path srcPath = getSavegameFile(e);

        FileUtils.forceMkdirParent(destPath.toFile());
        FileUtils.copyFile(srcPath.toFile(), destPath.toFile(), false);
        destPath.toFile().setLastModified(Instant.now().toEpochMilli());
    }

    public synchronized void melt(SavegameEntry<T,I> e) {
        if (e.getInfo() == null) {
            return;
        }

        if (!e.getInfo().isBinary()) {
            return;
        }

        try {
            var bytes = RakalyHelper.toMeltedPlaintext(getSavegameFile(e));
            var struc = type.determineStructure(bytes);
            var succ = struc.parse(bytes).orThrow();
            var c = succ.content;
            type.generateNewCampaignIdHeuristic(c);
            var targetCollection = type.getCampaignIdHeuristic(c);
            var info = infoFactory.apply(succ.content, false);
            var name = getSavegameCollection(e).getName() + " (" + PdxuI18n.get("MELTED") + ")";
            addEntryToCollection(targetCollection, file -> struc.write(file, c), null, info, null, name);
            saveData();
        } catch (Exception ex) {
            ErrorHandler.handleException(ex);
        }
    }

    protected Optional<SavegameParseResult> importSavegame(
            Path file,
            boolean checkDuplicate,
            String sourceFileChecksum,
            UUID customCampaignId) {
        var status = importSavegameData(file, checkDuplicate, sourceFileChecksum, customCampaignId);
        saveData();
        return status;
    }

    private String getSaveFileName() {
        return "savegame." + type.getFileEnding();
    }

    private String getInfoFileName() {
        String cs = IntegrityManager.getInstance().getChecksum(ALL.inverseBidiMap().get(this));
        return "info_" + cs + ".json";
    }

    public synchronized void invalidateSavegameInfo(SavegameEntry<T, I> e) {
        if (Files.exists(getSavegameInfoFile(e))) {
            logger.debug("Invalidating " + getSavegameInfoFile(e));
            try {
                Files.delete(getSavegameInfoFile(e));
            } catch (Exception ex) {
                ErrorHandler.handleException(ex);
            }
        }
    }

    private String checksum(byte[] content) {
        MessageDigest d = null;
        try {
            d = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 missing!");
        }
        d.update(content);
        StringBuilder c = new StringBuilder();
        ByteBuffer b = ByteBuffer.wrap(d.digest());
        for (int i = 0; i < 16; i++) {
            var hex = String.format("%02x", b.get());
            c.append(hex);
        }
        return c.toString();
    }

    private Optional<SavegameParseResult> importSavegameData(
            Path file,
            boolean checkDuplicate,
            String sourceFileChecksum,
            UUID customCampaignId) {
        logger.debug("Parsing file " + file.toString());
        final SavegameParseResult[] result = new SavegameParseResult[1];
        String checksum;
        byte[] bytes;
        boolean melted;
        try {
            bytes = Files.readAllBytes(file);
            checksum = checksum(bytes);
            logger.debug("Checksum is " + checksum);
            if (checkDuplicate) {
                var exists = getSavegameForChecksum(checksum);
                if (exists.isPresent()) {
                    logger.debug("Entry " + exists.get().getName() + " with checksum already in storage");
                    if (sourceFileChecksum != null) {
                        exists.get().addSourceFileChecksum(sourceFileChecksum);
                    }
                    return Optional.empty();
                } else {
                    logger.debug("No entry with checksum found");
                }
            }

            byte[] data;
            if (type.isBinary(bytes)) {
                data = RakalyHelper.toEquivalentPlaintext(file);
                melted = true;
            } else {
                data = bytes;
                melted = false;
            }
            var struc = type.determineStructure(data);
            result[0] = struc.parse(data);
        } catch (Exception ex) {
            return Optional.of(new SavegameParseResult.Error(ex));
        }

        final SavegameParseResult[] resultToReturn = new SavegameParseResult[1];
        result[0].visit(new SavegameParseResult.Visitor() {
            @Override
            public void success(SavegameParseResult.Success s) {
                logger.debug("Parsing was successful. Loading info ...");
                I info = null;
                try {
                    info = infoFactory.apply(s.content, melted);
                } catch (SavegameInfoException e) {
                    resultToReturn[0] = new SavegameParseResult.Error(e);
                    return;
                }

                if (customCampaignId != null) {
                    logger.debug("Using custom campaign id: " + customCampaignId);
                }
                var targetId = customCampaignId != null ? customCampaignId : type.getCampaignIdHeuristic(s.content);
                addEntryToCollection(targetId, file -> Files.write(file, bytes), checksum, info, null, null);
            }

            @Override
            public void error(SavegameParseResult.Error e) {
                logger.error("An error occured during parsing: " + e.error.getMessage());
                resultToReturn[0] = e;
            }

            @Override
            public void invalid(SavegameParseResult.Invalid iv) {
                logger.error("Savegame is invalid: " + iv.message);
                resultToReturn[0] = iv;
            }
        });
        return Optional.ofNullable(resultToReturn[0]);
    }

    private void addEntryToCollection(UUID campaignId, FailableConsumer<Path, Exception> writer, String checksum, I info, String sourceFileChecksum, String defaultCampaignName) {
        logger.debug("Campaign UUID is " + campaignId.toString());

        UUID saveUuid = UUID.randomUUID();
        logger.debug("Generated savegame UUID " + saveUuid.toString());

        Path entryPath = getSavegameDataDirectory().resolve(campaignId.toString()).resolve(saveUuid.toString());
        try {
            FileUtils.forceMkdir(entryPath.toFile());
            var file = entryPath.resolve(getSaveFileName());
            writer.accept(file);
            JsonHelper.writeObject(info, entryPath.resolve(getInfoFileName()));

            addNewEntryToCampaign(campaignId, saveUuid, checksum, info, null, sourceFileChecksum, defaultCampaignName);
        } catch (Exception e) {
            ErrorHandler.handleException(e);
        }
    }

    synchronized void createNewBranch(SavegameEntry<T,I> e) {
        if (!e.isLoaded()) {
            return;
        }

        byte[] bytes;
        String checksum;
        try {
            bytes = Files.readAllBytes(getSavegameFile(e));
            checksum = checksum(bytes);
            if (type.isBinary(bytes)) {
                bytes = RakalyHelper.toEquivalentPlaintext(getSavegameFile(e));
            }
        } catch (Exception ex) {
            ErrorHandler.handleException(ex);
            return;
        }

        var struc = type.determineStructure(bytes);
        var r = struc.parse(bytes);
        if (r.success().isEmpty()) {
            return;
        }

        var s = r.success().get();
        var c = s.content;

        UUID targetId;
        FailableConsumer<Path, Exception> writer;
        if (!e.getInfo().isBinary() && !e.getInfo().isIronman()) {
            // Update savegame information itself if possible
            struc.getType().generateNewCampaignIdHeuristic(c);
            targetId = struc.getType().getCampaignIdHeuristic(c);
            writer = file -> struc.write(file, c);
        } else {
            // Use random id otherwise
            targetId = UUID.randomUUID();
            writer = file -> Files.copy(getSavegameFile(e), file);
        }

        // Generate new info
        I info;
        try {
            info = infoFactory.apply(s.content, e.getInfo().isBinary());
        } catch (SavegameInfoException ex) {
            ErrorHandler.handleException(ex);
            return;
        }

        var sourceName = getSavegameCollection(e).getName();
        var newName = sourceName + " (" + PdxuI18n.get("NEW_BRANCH") + ")";
        addEntryToCollection(targetId, writer, checksum, info, null, newName);
        saveData();
    }

    public synchronized Optional<SavegameEntry<T, I>> getSavegameForChecksum(String cs) {
        return getCollections().stream().flatMap(SavegameCollection::entryStream)
                .filter(ch -> ch.getContentChecksum().equals(cs))
                .findAny();
    }

    public synchronized String getEntryName(SavegameEntry<T, I> e) {
        String cn = getSavegameCollection(e).getName();
        String en = e.getName();
        return cn + " (" + en + ")";
    }

    public synchronized Optional<SavegameEntry<T,I>> getEntryForSourceFileChecksum(String sourceFileChecksum) {
        return getCollections().stream().flatMap(SavegameCollection::entryStream)
                .filter(ch -> ch.getSourceFileChecksums().contains(sourceFileChecksum))
                .findAny();
    }

    public synchronized boolean hasImportedSourceFile(String sourceFileChecksum) {
        return getCollections().stream().flatMap(SavegameCollection::entryStream)
                .anyMatch(ch -> ch.getSourceFileChecksums().contains(sourceFileChecksum));
    }

    public Path getSavegameDataDirectory() {
        return path;
    }

    public Path getDataFile() {
        return getSavegameDataDirectory().resolve("campaigns.json");
    }

    public ObservableSet<SavegameCollection<T, I>> getCollections() {
        return collections;
    }

    public String getName() {
        return name;
    }

    public SavegameType getType() {
        return type;
    }
}

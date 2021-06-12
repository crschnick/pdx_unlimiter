package com.crschnick.pdxu.app.savegame;

import com.crschnick.pdxu.app.core.ErrorHandler;
import com.crschnick.pdxu.app.core.IntegrityManager;
import com.crschnick.pdxu.app.core.settings.Settings;
import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.app.lang.LanguageManager;
import com.crschnick.pdxu.app.util.ConfigHelper;
import com.crschnick.pdxu.app.util.JsonHelper;
import com.crschnick.pdxu.app.util.integration.RakalyHelper;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.parser.ParseException;
import com.crschnick.pdxu.io.savegame.SavegameParseResult;
import com.crschnick.pdxu.io.savegame.SavegameStructure;
import com.crschnick.pdxu.io.savegame.SavegameType;
import com.crschnick.pdxu.model.GameDateType;
import com.crschnick.pdxu.model.SavegameInfo;
import com.crschnick.pdxu.model.SavegameInfoException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.function.FailableBiFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

public abstract class SavegameStorage<T, I extends SavegameInfo<T>> {


    public static final BidiMap<Game, SavegameStorage<?, ?>> ALL = new DualHashBidiMap<>();
    private final Logger logger;
    private final Class<I> infoClass;
    private final FailableBiFunction<Node, Boolean, I, SavegameInfoException> infoFactory;
    private final String name;
    private final GameDateType dateType;
    private final Path path;
    private final SavegameType type;
    private final ObservableSet<SavegameCollection<T, I>> collections = FXCollections.observableSet(new HashSet<>());

    public SavegameStorage(
            FailableBiFunction<Node, Boolean, I, SavegameInfoException> infoFactory,
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
        SavegameStorages.init();
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

            JsonNode c = node.required("campaigns");
            for (int i = 0; i < c.size(); i++) {
                SavegameCampaignFactory.parse(this, c.get(i)).ifPresent(cam -> {
                    collections.add(cam);
                });
            }


            // Legacy support, can be missing
            JsonNode f = Optional.ofNullable(node.get("folders")).orElse(JsonNodeFactory.instance.arrayNode());
            for (int i = 0; i < f.size(); i++) {
                SavegameCampaignFactory.parse(this, f.get(i)).ifPresent(cam -> {
                    collections.add(cam);
                });
            }

        for (SavegameCollection<T, I> collection : collections) {
            collection.deserializeEntries();
        }
    }

    private synchronized void saveData() {
        ObjectNode n = JsonNodeFactory.instance.objectNode();

        ArrayNode c = n.putArray("campaigns");
        getCollections().forEach(campaign -> {
            campaign.saveData();
            JsonNode campaignNode = campaign.serialize();
            c.add(campaignNode);
        });

        ConfigHelper.writeConfig(getDataFile(), n);
    }

    String getDefaultEntryName(I info) {
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

        saveData();
    }

    synchronized void delete(SavegameEntry<T, I> e) {
        SavegameCollection<T, I> c = getSavegameCollection(e);
        if (!this.collections.contains(c) || !c.getSavegames().contains(e)) {
            return;
        }

        if (c.delete(e)) {
            if (c.getSavegames().size() == 0) {
                delete(c);
                saveData();
            }
        }
    }

    synchronized void move(SavegameCollection<T,I> target, SavegameEntry<T, I> entry) {
        if (target.getSavegames().contains(entry)) {
            return;
        }

        var source = getSavegameCollection(entry);
        if (target.copyTo(entry)) {
            source.delete(entry);
        }
    }

    public synchronized void createNewBranch(
            SavegameEntry<T,I> e) {
        SavegameCampaignFactory.createNewBranch(this, e).ifPresent(cam -> {
            this.collections.add(cam);
        });
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
                bytes = RakalyHelper.toPlaintext(file);
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
                    I info = infoFactory.apply(s.combinedNode(), melted);
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
                ErrorHandler.handleException(new ParseException(iv.message), null, file);
            }
        });
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

    public synchronized String getCollectionFileName(SavegameCollection<?, ?> c) {
        var name = c.getName();
        return SavegameContext.getForCollection(c).getInstallType().getCompatibleSavegameName(name);
    }

    public synchronized String getEntryFileName(SavegameEntry<?, ?> e) {
        var name = e.getName();
        return SavegameContext.getForSavegame(e).getInstallType().getCompatibleSavegameName(name);
    }

    public synchronized String getCompatibleName(SavegameEntry<?, ?> e, boolean includeEntryName) {
        var name = getSavegameCollection(e).getName() + (includeEntryName ?
                " (" + e.getName() + ")." : ".") + type.getFileEnding();
        return SavegameContext.getForSavegame(e).getInstallType().getCompatibleSavegameName(name);
    }

    public synchronized void copySavegameTo(SavegameEntry<T, I> e, Path destPath) throws IOException {
        Path srcPath = getSavegameFile(e);

        FileUtils.forceMkdirParent(destPath.toFile());
        FileUtils.copyFile(srcPath.toFile(), destPath.toFile(), false);
        destPath.toFile().setLastModified(Instant.now().toEpochMilli());
    }

    protected Optional<SavegameParseResult> importSavegame(
            Path file,
            String name,
            boolean checkDuplicate,
            String sourceFileChecksum,
            Long branchId) {
        var status = importSavegameData(file, name, checkDuplicate, sourceFileChecksum, branchId);
        saveData();
        return status;
    }

    String getSaveFileName() {
        return "savegame." + type.getFileEnding();
    }

    String getInfoFileName() {
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

    void reloadSavegame(SavegameEntry<T, I> e) {
        logger.debug("Reloading savegame");
        e.unload();

        invalidateSavegameInfo(e);

        loadEntry(e);
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
            String name,
            boolean checkDuplicate,
            String sourceFileChecksum,
            Long branchId) {
        logger.debug("Parsing file " + file.toString());
        SavegameParseResult result;
        String checksum;
        byte[] bytes;
        boolean melted;
        SavegameStructure struc;
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
                data = RakalyHelper.toPlaintext(file);
                melted = true;
            } else {
                data = bytes;
                melted = false;
            }
            struc = type.determineStructure(data);
            result = struc.parse(data);
        } catch (Exception ex) {
            return Optional.of(new SavegameParseResult.Error(ex));
        }


        if (result.success().isPresent()) {
            SavegameParseResult.Success s = result.success().get();
            logger.debug("Parsing was successful. Loading info ...");
            I info;
            try {
                info = infoFactory.apply(s.combinedNode(), melted);
            } catch (SavegameInfoException e) {
                return Optional.of(new SavegameParseResult.Error(e));
            }

            UUID collectionUuid;
            if (info.isIronman() && branchId != null) {
                var campaign = getCampaignForBranchId(branchId)
                        .orElseThrow(() -> new IllegalArgumentException("Unknown branch id"));
                collectionUuid = campaign.getUuid();
                logger.debug("Savegame has branch id in file name");
            } else {
                collectionUuid = struc.getCampaignIdHeuristic(s.content);
            }

            logger.debug("Collection UUID is " + collectionUuid.toString());
            getSavegameCollection(collectionUuid).ifPresentOrElse(col -> {
                logger.debug("Adding new entry to existing collection");
                col.addNewEntry(checksum, info, name, sourceFileChecksum);
            }, () -> {
                Optional<SavegameCampaign<T,I>> campaign = SavegameCampaignFactory.importSavegameData(
                        this, collectionUuid, info, bytes, checksum, sourceFileChecksum);
                campaign.ifPresent(SavegameStorage.this.collections::add);
            });
            return Optional.empty();
        } else {
            result.visit(new SavegameParseResult.Visitor() {
                @Override
                public void error(SavegameParseResult.Error e) {
                    logger.error("An error occured during parsing: " + e.error.getMessage());
                }

                @Override
                public void invalid(SavegameParseResult.Invalid iv) {
                    logger.error("Savegame is invalid: " + iv.message);
                }
            });
            return Optional.of(result);
        }
    }

    public synchronized Optional<SavegameEntry<T, I>> getSavegameForChecksum(String cs) {
        return getCollections().stream().flatMap(SavegameCollection::entryStream)
                .filter(ch -> ch.getContentChecksum().equals(cs))
                .findAny();
    }

    public synchronized Optional<SavegameCollection<T, I>> getCampaignForBranchId(long id) {
        return getCollections().stream().filter(col -> {
            if (col instanceof SavegameCampaign<T, I> campaign) {
                return campaign.isBranch() && campaign.getBranchId() == id;
            }
            return false;
        }).findAny();
    }

    public String getEntryName(SavegameEntry<T, I> e) {
        String cn = getSavegameCollection(e).getName();
        String en = e.getName();
        return cn + " (" + en + ")";
    }

    public Optional<SavegameEntry<T,I>> getEntryForSourceFile(String sourceFileChecksum) {
        return getCollections().stream().flatMap(SavegameCollection::entryStream)
                .filter(ch -> ch.getSourceFileChecksums().contains(sourceFileChecksum))
                .findAny();
    }

    public boolean hasImportedSourceFile(String sourceFileChecksum) {
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

    public FailableBiFunction<Node, Boolean, I, SavegameInfoException> getInfoFactory() {
        return infoFactory;
    }

    public GameDateType getDateType() {
        return dateType;
    }
}

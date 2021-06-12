package com.crschnick.pdxu.app.savegame;

import com.crschnick.pdxu.app.core.ErrorHandler;
import com.crschnick.pdxu.app.gui.game.GameGuiFactory;
import com.crschnick.pdxu.app.gui.game.ImageLoader;
import com.crschnick.pdxu.app.util.JsonHelper;
import com.crschnick.pdxu.model.GameDate;
import com.crschnick.pdxu.model.SavegameInfo;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.scene.image.Image;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

public class SavegameCampaignFactory {

    private static final Logger logger = LoggerFactory.getLogger(SavegameCampaignFactory.class);

    public static <T,I extends SavegameInfo<T>> Optional<SavegameCampaign<T,I>> parse(SavegameStorage<T,I> storage, JsonNode c) {
        String name = c.required("name").textValue();
        GameDate date = Optional.ofNullable(c.get("date")).map(d -> storage.getDateType().fromString(d.textValue()))
                .orElse(storage.getDateType().fromLong(0));
        UUID id = UUID.fromString(c.required("uuid").textValue());
        if (!Files.isDirectory(storage.getSavegameDataDirectory().resolve(id.toString()))) {
            return Optional.empty();
        }

        Instant lastDate = Instant.parse(c.required("lastPlayed").textValue());
        Image image = ImageLoader.loadImage(storage.getSavegameDataDirectory()
                .resolve(id.toString()).resolve("campaign.png"));
        Integer branchId = Optional.ofNullable(c.get("branchId")).map(JsonNode::intValue).orElse(null);
        return Optional.of(new SavegameCampaign<>(storage, lastDate, name, id, branchId, date, image));
    }


    public static <T,I extends SavegameInfo<T>> Optional<SavegameCampaign<T,I>> createNewBranch(
            SavegameStorage<T,I> storage,
            SavegameEntry<T,I> e) {
        if (e.getInfo() == null) {
            return Optional.empty();
        }

        if (e.getInfo().isBinary() && !e.getInfo().isIronman()) {
            return Optional.empty();
        }

        byte[] bytes;
        try {
            bytes = Files.readAllBytes(storage.getSavegameFile(e));
        } catch (IOException ioException) {
            ErrorHandler.handleException(
                    ioException, "Couldn't parse savegame " + storage.getSavegameFile(e), storage.getSavegameFile(e));
            return Optional.empty();
        }

        var type = storage.getType();
        var binary = type.isBinary(bytes);
        logger.debug("Adding new branch. Rewrite savegame=" + !binary);
        if (binary) {
            int newId = new Random().nextInt(Integer.MAX_VALUE);
            logger.debug("Adding new branch with id " + newId);
            var campaignUuid = UUID.randomUUID();
            var entryUuid = UUID.randomUUID();

            try {
                Path entryPath = storage.getSavegameDataDirectory().resolve(campaignUuid.toString()).resolve(entryUuid.toString());
                FileUtils.forceMkdir(entryPath.toFile());
                var file = entryPath.resolve(storage.getSaveFileName());
                Files.copy(storage.getSavegameFile(e), file);
                JsonHelper.writeObject(e.getInfo(), entryPath.resolve(storage.getInfoFileName()));

                var campaign = new SavegameCampaign<>(storage, Instant.now(),
                        storage.getDefaultCampaignName(e.getInfo()),campaignUuid, newId,
                        e.getDate(), SavegameActions.createImageForEntry(e));
                campaign.savegames.add(new SavegameEntry<>(e, entryUuid));
                campaign.saveData();
                return Optional.of(campaign);
            } catch (IOException ex) {
                ErrorHandler.handleException(ex);
                return Optional.empty();
            }
        } else {
            var struc = type.determineStructure(bytes);
            var r = struc.parse(bytes);
            if (r.success().isEmpty()) {
                return Optional.empty();
            }

            try {
                var c = r.success().get().content;
                int branchId = new Random().nextInt(Integer.MAX_VALUE);
                logger.debug("Adding new branch with id " + branchId);
                struc.generateNewCampaignIdHeuristic(c, branchId);
                UUID uuid = struc.getCampaignIdHeuristic(c);
                logger.debug("Campaign id generated from branch id is " + uuid);

                var entryUuid = UUID.randomUUID();
                Path entryPath = storage.getSavegameDataDirectory().resolve(uuid.toString()).resolve(entryUuid.toString());
                FileUtils.forceMkdir(entryPath.toFile());
                var file = entryPath.resolve(storage.getSaveFileName());
                struc.write(file, c);
                JsonHelper.writeObject(e.getInfo(), entryPath.resolve(storage.getInfoFileName()));

                var campaign = new SavegameCampaign<>(storage, Instant.now(),
                        storage.getDefaultCampaignName(e.getInfo()), uuid, branchId, e.getDate(),
                        SavegameActions.createImageForEntry(e));
                campaign.savegames.add(new SavegameEntry<>(e, entryUuid));
                campaign.saveData();
                return Optional.of(campaign);
            } catch (Exception ex) {
                ErrorHandler.handleException(ex, "Couldn't create campaign branch", storage.getSavegameFile(e));
                return Optional.empty();
            }
        }
    }

    private static <T,I extends SavegameInfo<T>>  Image createImage(SavegameStorage<T,I> storage, I info) {
        var img = GameGuiFactory.<T, I>get(SavegameStorage.ALL.inverseBidiMap().get(storage))
                .tagImage(info, info.getTag());
        return img;
    }

    public static <T,I extends SavegameInfo<T>> Optional<SavegameCampaign<T,I>> importSavegameData(
            SavegameStorage<T,I> storage,
            UUID campaignUuid,
            I info,
            byte[] bytes,
            String checksum,
            String sourceFileChecksum) {
        var campaignPath = storage.getSavegameDataDirectory()
                .resolve(campaignUuid.toString());

        try {
            var entryUuid = UUID.randomUUID();
            Path entryPath = campaignPath.resolve(entryUuid.toString());
            FileUtils.forceMkdir(entryPath.toFile());
            var file = entryPath.resolve(storage.getSaveFileName());
            Files.write(file, bytes);
            JsonHelper.writeObject(info, entryPath.resolve(storage.getInfoFileName()));

            var campaign = new SavegameCampaign<>(storage, Instant.now(),
                    storage.getDefaultCampaignName(info), campaignUuid, null, info.getDate(),
                    createImage(storage, info));
            campaign.savegames.add(new SavegameEntry<T,I>(info.getDate().toString(), entryUuid, checksum, info.getDate(), null, sourceFileChecksum != null ? List.of(sourceFileChecksum) : List.of()));
            campaign.saveData();
            return Optional.of(campaign);
        } catch (Exception e) {
            ErrorHandler.handleException(e);
            return Optional.empty();
        }
    }
}

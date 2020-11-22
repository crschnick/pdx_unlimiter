package com.crschnick.pdx_unlimiter.app.savegame;

import com.crschnick.pdx_unlimiter.app.game.*;
import com.crschnick.pdx_unlimiter.app.installation.ErrorHandler;
import com.crschnick.pdx_unlimiter.eu4.parser.GameDate;
import com.crschnick.pdx_unlimiter.eu4.parser.Hoi4Date;
import com.crschnick.pdx_unlimiter.eu4.parser.Hoi4Tag;
import com.crschnick.pdx_unlimiter.eu4.savegame.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class Hoi4SavegameCache extends SavegameCache<Hoi4SavegameInfo, Hoi4CampaignEntry, Hoi4Campaign> {
    public Hoi4SavegameCache() {
        super("hoi4");
    }

    private static final String SAVE_NAME = "savegame.hoi4";

    @Override
    protected void updateCampaignProperties(Hoi4Campaign c) {
        c.getSavegames().stream()
                .filter(s -> s.infoProperty().isNotNull().get())
                .map(s -> s.getInfo().getDate()).min(Comparator.naturalOrder())
                .ifPresent(d -> c.dateProperty().setValue(d));

        c.getSavegames().stream()
                .filter(s -> s.infoProperty().isNotNull().get())
                .min(Comparator.naturalOrder())
                .ifPresent(e -> c.tagProperty().setValue(e.getInfo().getTag()));
    }

    @Override
    protected Hoi4CampaignEntry readEntry(JsonNode node, String name, UUID uuid, String checksum) {
        Hoi4Tag tag = new Hoi4Tag(node.required("tag").textValue(), node.required("ideology").textValue());
        String date = node.required("date").textValue();
        return new Hoi4CampaignEntry(name, uuid, null, checksum, tag, Hoi4Date.fromString(date));
    }

    @Override
    protected Hoi4Campaign readCampaign(JsonNode node, String name, UUID uuid, Instant lastPlayed) {
        Hoi4Tag tag = new Hoi4Tag(node.required("tag").textValue(), node.required("ideology").textValue());
        String date = node.required("date").textValue();
        return new Hoi4Campaign(lastPlayed, name, uuid, tag, Hoi4Date.fromString(date));
    }

    @Override
    protected void writeEntry(ObjectNode node, Hoi4CampaignEntry entry) {


        node.put("tag", entry.getTag().getTag())
        .put("ideology", entry.getTag().getIdeology())
                .put("date", entry.getDate().toString());
    }

    @Override
    protected void writeCampaign(ObjectNode node, Hoi4Campaign campaign) {

        node.put("tag", campaign.getTag().getTag())
                .put("ideology", campaign.getTag().getIdeology())
                .put("date", campaign.getDate().toString());
    }

    @Override
    protected Hoi4Campaign createCampaign(Hoi4SavegameInfo info) {
        return new Hoi4Campaign(Instant.now(),
                GameInstallation.HOI4.getCountryNames().getOrDefault(info.getTag(), "Unknown")
                , info.getCampaignUuid(),
                info.getTag(),
                info.getDate());
    }

    @Override
    protected Hoi4CampaignEntry createEntry(UUID uuid, String checksum, Hoi4SavegameInfo info) {
        return new Hoi4CampaignEntry(
                info.getDate().toString(), uuid,
                info, checksum, info.getTag(), info.getDate());
    }

    @Override
    protected void writeSavegameData(Path savegame, Path out) throws Exception {
        Hoi4Savegame is = null;
        Hoi4SavegameInfo e = null;

        Hoi4RawSavegame save = Hoi4RawSavegame.fromFile(savegame);
        is = Hoi4Savegame.fromSavegame(save);
        is.write(out, true);
    }

    @Override
    protected boolean needsUpdate(Hoi4CampaignEntry hoi4CampaignEntry) throws Exception {
        Path p = getPath(hoi4CampaignEntry);
        int v = 0;
        try {
            v = p.toFile().exists() ? Hoi4Savegame.getVersion(p.resolve("data.zip")) : 0;
        } catch (Exception ex) {
            ErrorHandler.handleException(ex);
            return true;
        }

        return v < Hoi4Savegame.VERSION;
    }

    @Override
    protected Hoi4SavegameInfo loadInfo(Path p) throws Exception {
        Hoi4Savegame sg = Hoi4Savegame.fromFile(p);
        Hoi4SavegameInfo info = Hoi4SavegameInfo.fromSavegame(sg);
        return info;
    }

    @Override
    protected void importSavegameData(Path file) throws Exception {

        Hoi4Savegame is = null;
        Hoi4SavegameInfo e = null;

        Hoi4RawSavegame save = Hoi4RawSavegame.fromFile(file);
        is = Hoi4Savegame.fromSavegame(save);
        e = Hoi4SavegameInfo.fromSavegame(is);

        UUID uuid = e.getCampaignUuid();

        Optional<Hoi4Campaign> existing = getCampaigns().stream()
                .filter(c -> c.getCampaignId().equals(uuid))
                .findAny();

        if (existing.isPresent()) {
            Optional<Hoi4CampaignEntry> existingEntry = existing.get().getSavegames().stream()
                    .filter(c -> c.getChecksum().equals(save.getFileChecksum()))
                    .findAny();
            if (existingEntry.isPresent()) {
                FileUtils.forceDelete(file.toFile());
                GameIntegration.selectIntegration(GameIntegration.HOI4);
                GameIntegration.current().selectEntry(existingEntry.get());
                return;
            }
        }


        UUID saveUuid = UUID.randomUUID();
        Path campaignPath = getPath().resolve(uuid.toString());
        Path entryPath = campaignPath.resolve(saveUuid.toString());

        FileUtils.forceMkdir(entryPath.toFile());
        is.write(entryPath.resolve("data.zip"), true);
        FileUtils.copyFile(file.toFile(), getBackupPath().resolve(file.getFileName()).toFile());
        FileUtils.moveFile(file.toFile(), entryPath.resolve(SAVE_NAME).toFile());
        Hoi4CampaignEntry entry = this.addNewEntry(uuid, saveUuid, save.getFileChecksum(), e);
    }
}

package com.crschnick.pdx_unlimiter.app.savegame;

import com.crschnick.pdx_unlimiter.app.game.*;
import com.crschnick.pdx_unlimiter.app.installation.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.installation.Settings;
import com.crschnick.pdx_unlimiter.eu4.data.Eu4Date;
import com.crschnick.pdx_unlimiter.eu4.savegame.Eu4RawSavegame;
import com.crschnick.pdx_unlimiter.eu4.savegame.Eu4Savegame;
import com.crschnick.pdx_unlimiter.eu4.savegame.Eu4SavegameInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.FileUtils;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Comparator;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class Eu4SavegameCache extends SavegameCache<Eu4Savegame, Eu4SavegameInfo, Eu4CampaignEntry, Eu4Campaign> {

    public Eu4SavegameCache() {
        super("eu4");
    }

    @Override
    protected void updateCampaignProperties(Eu4Campaign c) {
        c.getSavegames().stream()
                .filter(s -> s.infoProperty().isNotNull().get())
                .map(s -> s.getInfo().getDate()).min(Comparator.naturalOrder())
                .ifPresent(d -> c.dateProperty().setValue(d));

        c.getSavegames().stream()
                .filter(s -> s.infoProperty().isNotNull().get())
                .min(Comparator.comparingLong(ca -> Eu4Date.toLong(c.getDate())))
                .ifPresent(e -> c.tagProperty().setValue(e.getInfo().getCurrentTag().getTag()));
    }

    @Override
    protected Eu4Campaign createNewCampaignForEntry(Eu4CampaignEntry entry) {
        return new Eu4Campaign(
                Instant.now(),
                GameLocalisation.getTagNameForEntry(entry, entry.getInfo().getCurrentTag()),
                entry.getInfo().getCampaignUuid(),
                entry.getInfo().getCurrentTag().getTag(),
                entry.getInfo().getDate());
    }

    @Override
    protected Eu4CampaignEntry createEntry(UUID uuid, String checksum, Eu4SavegameInfo info) {
        return new Eu4CampaignEntry(
                info.getDate().toDisplayString(),
                uuid,
                info,
                checksum,
                info.getCurrentTag().getTag(),
                info.getDate());
    }

    @Override
    protected void writeSavegameData(Path savegame, Path out) throws Exception {
        Eu4RawSavegame save = Eu4RawSavegame.fromFile(savegame);
        Eu4Savegame sg = Eu4Savegame.fromSavegame(save);
        sg.write(out, true);
    }

    @Override
    protected boolean needsUpdate(Eu4CampaignEntry eu4CampaignEntry) throws Exception {
        Path p = getPath(eu4CampaignEntry);
        int v = 0;
        try {
            v = p.toFile().exists() ? Eu4Savegame.getVersion(p.resolve("data.zip")) : 0;
        } catch (Exception ex) {
            ErrorHandler.handleException(ex);
            return true;
        }

        return v < Eu4Savegame.VERSION;
    }

    @Override
    protected Eu4SavegameInfo loadInfo(Eu4Savegame data) throws Exception {
        return Eu4SavegameInfo.fromSavegame(data);
    }

    @Override
    protected Eu4Savegame loadData(Path p) throws Exception {
        return Eu4Savegame.fromFile(p);
    }

    @Override
    protected void importSavegameData(Path file) throws Exception {
        Eu4Savegame savegame = null;
        Eu4SavegameInfo info = null;

        Eu4RawSavegame save = Eu4RawSavegame.fromFile(file);
        savegame = Eu4Savegame.fromSavegame(save);
        info = Eu4SavegameInfo.fromSavegame(savegame);

        UUID uuid = info.getCampaignUuid();

        AtomicBoolean exists = new AtomicBoolean(false);
        getCampaigns().stream()
                .filter(c -> c.getCampaignId().equals(uuid))
                .findAny().ifPresent(c -> exists.set(c.getSavegames().stream()
                .map(GameCampaignEntry::getChecksum).anyMatch(ch -> ch.equals(save.getFileChecksum()))));
        if (exists.get()) {
            if (Settings.getInstance().deleteOnImport()) FileUtils.forceDelete(file.toFile());
            return;
        }


        UUID saveUuid = UUID.randomUUID();
        Path campaignPath = getPath().resolve(uuid.toString());
        Path entryPath = campaignPath.resolve(saveUuid.toString());

        FileUtils.forceMkdir(entryPath.toFile());
        savegame.write(entryPath.resolve("data.zip"), true);
        if (Settings.getInstance().deleteOnImport()) {
            FileUtils.copyFile(file.toFile(), getBackupPath().resolve(file.getFileName()).toFile());
            FileUtils.moveFile(file.toFile(), entryPath.resolve("savegame.eu4").toFile());
        } else {
            FileUtils.copyFile(file.toFile(), entryPath.resolve("savegame.eu4").toFile());
        }
        this.addNewEntry(uuid, saveUuid, save.getFileChecksum(), info, savegame);
    }

    @Override
    protected Eu4CampaignEntry readEntry(JsonNode node, String name, UUID uuid, String checksum) {
        String tag = node.required("tag").textValue();
        String date = node.required("date").textValue();
        return new Eu4CampaignEntry(name, uuid, null, checksum, tag, Eu4Date.fromString(date));
    }

    @Override
    protected Eu4Campaign readCampaign(JsonNode node, String name, UUID uuid, Instant lastPlayed) {
        String tag = node.required("tag").textValue();
        String lastDate = node.required("date").textValue();
        return new Eu4Campaign(lastPlayed, name, uuid, tag, Eu4Date.fromString(lastDate));
    }

    @Override
    protected void writeEntry(ObjectNode node, Eu4CampaignEntry entry) {

        node.put("tag", entry.getTag())
                .put("date", entry.getDate().toString());
    }

    @Override
    protected void writeCampaign(ObjectNode node, Eu4Campaign campaign) {

        node.put("tag", campaign.getTag())
                .put("date", campaign.getDate().toString());
    }
}

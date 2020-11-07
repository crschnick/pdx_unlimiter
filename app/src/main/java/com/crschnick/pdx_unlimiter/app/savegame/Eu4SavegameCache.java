package com.crschnick.pdx_unlimiter.app.savegame;

import com.crschnick.pdx_unlimiter.app.game.*;
import com.crschnick.pdx_unlimiter.app.installation.ErrorHandler;
import com.crschnick.pdx_unlimiter.eu4.Eu4IntermediateSavegame;
import com.crschnick.pdx_unlimiter.eu4.Eu4SavegameInfo;
import com.crschnick.pdx_unlimiter.eu4.parser.Eu4Savegame;
import com.crschnick.pdx_unlimiter.eu4.parser.GameDate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.FileUtils;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Comparator;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class Eu4SavegameCache extends SavegameCache<Eu4SavegameInfo, Eu4CampaignEntry, Eu4Campaign> {

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
                .min(Comparator.comparingLong(ca -> GameDate.toLong(c.getDate())))
                .ifPresent(e -> c.tagProperty().setValue(e.getInfo().getCurrentTag().getTag()));
    }

    @Override
    protected Eu4Campaign createCampaign(Eu4SavegameInfo info) {
        return new Eu4Campaign(Instant.now(),
                info.getCurrentTag().isCustom() ?
                        info.getCurrentTag().getName() : GameInstallation.EU4.getCountryName(info.getCurrentTag())
                , info.getCampaignUuid(),
                info.getCurrentTag().getTag(),
                info.getDate());
    }

    @Override
    protected Eu4CampaignEntry createEntry(UUID uuid, String checksum, Eu4SavegameInfo info) {
        return new Eu4CampaignEntry(
                null, uuid,
                info, checksum, info.getCurrentTag().getTag(), info.getDate());
    }

    @Override
    protected boolean needsUpdate(Eu4CampaignEntry eu4CampaignEntry) throws Exception {
        Path p = getPath(eu4CampaignEntry);
        int v = 0;
        try {
            v = p.toFile().exists() ? Eu4IntermediateSavegame.getVersion(p.resolve("data.zip")) : 0;
        } catch (Exception ex) {
            ErrorHandler.handleException(ex);
            return true;
        }

        return v < Eu4IntermediateSavegame.VERSION;
    }

    @Override
    protected void importSavegameData(Path file) throws Exception {
        Eu4IntermediateSavegame is = null;
        Eu4SavegameInfo e = null;

        Eu4Savegame save = Eu4Savegame.fromFile(file);
        is = Eu4IntermediateSavegame.fromSavegame(save);
        e = Eu4SavegameInfo.fromSavegame(is);

        UUID uuid = e.getCampaignUuid();

        AtomicBoolean exists = new AtomicBoolean(false);
        getCampaigns().stream()
                .filter(c -> c.getCampaignId().equals(uuid))
                .findAny().ifPresent(c -> exists.set(c.getSavegames().stream()
                .map(GameCampaignEntry::getChecksum).anyMatch(ch -> ch.equals(save.getFileChecksum()))));
        if (exists.get()) {
            FileUtils.forceDelete(file.toFile());
            return;
        }


        UUID saveUuid = UUID.randomUUID();
        Path campaignPath = getPath().resolve(uuid.toString());
        Path entryPath = campaignPath.resolve(saveUuid.toString());

        FileUtils.forceMkdir(entryPath.toFile());
        is.write(entryPath.resolve("data.zip"), true);
        FileUtils.copyFile(file.toFile(), getBackupPath().resolve(file.getFileName()).toFile());
        FileUtils.moveFile(file.toFile(), entryPath.resolve("savegame.eu4").toFile());
        this.addNewEntry(uuid, saveUuid, save.getFileChecksum(), e);
    }

    @Override
    protected Eu4SavegameInfo loadInfo(Path p) throws Exception {
        Eu4IntermediateSavegame is = null;
        Eu4SavegameInfo e = null;
        is = Eu4IntermediateSavegame.fromFile(p);
        e = Eu4SavegameInfo.fromSavegame(is);
        return e;
    }

    @Override
    protected Eu4CampaignEntry readEntry(JsonNode node, String name, UUID uuid, String checksum) {
        String tag = node.required("tag").textValue();
        String date = node.required("date").textValue();
        return new Eu4CampaignEntry(name, uuid, null, checksum, tag, GameDate.fromString(date));
    }

    @Override
    protected Eu4Campaign readCampaign(JsonNode node, String name, UUID uuid, Instant lastPlayed) {
        String tag = node.required("tag").textValue();
        String lastDate = node.required("date").textValue();
        return new Eu4Campaign(lastPlayed, name, uuid, tag, GameDate.fromString(lastDate));
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

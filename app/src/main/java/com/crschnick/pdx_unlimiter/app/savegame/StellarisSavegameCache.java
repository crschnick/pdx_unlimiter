package com.crschnick.pdx_unlimiter.app.savegame;

import com.crschnick.pdx_unlimiter.app.game.*;
import com.crschnick.pdx_unlimiter.app.installation.ErrorHandler;
import com.crschnick.pdx_unlimiter.eu4.data.GameDate;
import com.crschnick.pdx_unlimiter.eu4.data.GameDateType;
import com.crschnick.pdx_unlimiter.eu4.data.StellarisTag;
import com.crschnick.pdx_unlimiter.eu4.savegame.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Comparator;
import java.util.UUID;

public class StellarisSavegameCache extends SavegameCache<StellarisRawSavegame, StellarisSavegame,
        StellarisTag,
        StellarisSavegameInfo> {
    public StellarisSavegameCache() {
        super("stellaris", "sav", GameDateType.STELLARIS);
    }

    @Override
    protected GameCampaignEntry<StellarisTag, StellarisSavegameInfo> readEntry(JsonNode node, String name, UUID uuid, String checksum, GameDate date) {
        StellarisTag tag = new StellarisTag(
                name,
                node.required("iconCategory").textValue(),
                node.required("iconFile").textValue(),
                node.required("backgroundCategory").textValue(),
                node.required("backgroundFile").textValue(),
                node.required("backgroundPrimaryColor").textValue(),
                node.required("backgroundSecondaryColor").textValue());
        return new GameCampaignEntry<>(name, uuid, null, checksum, date, tag);
    }

    @Override
    protected GameCampaign<StellarisTag, StellarisSavegameInfo> readCampaign(JsonNode node, String name, UUID uuid, Instant lastPlayed, GameDate date) {
        StellarisTag tag = new StellarisTag(
                name,
                node.required("iconCategory").textValue(),
                node.required("iconFile").textValue(),
                node.required("backgroundCategory").textValue(),
                node.required("backgroundFile").textValue(),
                node.required("backgroundPrimaryColor").textValue(),
                node.required("backgroundSecondaryColor").textValue());
        return new GameCampaign<>(lastPlayed, name, uuid, date, tag);
    }

    @Override
    protected void writeEntry(ObjectNode node, GameCampaignEntry<StellarisTag, StellarisSavegameInfo> stellarisCampaignEntry) {
        StellarisTag tag = stellarisCampaignEntry.getTag();
        node.put("iconCategory", tag.getIconCategory());
        node.put("iconFile", tag.getIconFile());
        node.put("backgroundCategory", tag.getBackgroundCategory());
        node.put("backgroundFile", tag.getBackgroundFile());
        node.put("backgroundPrimaryColor", tag.getBackgroundPrimaryColor());
        node.put("backgroundSecondaryColor", tag.getBackgroundSecondaryColor());
    }

    @Override
    protected void writeCampaign(ObjectNode node, GameCampaign<StellarisTag, StellarisSavegameInfo> c) {
        StellarisTag tag = c.getTag();
        node.put("iconCategory", tag.getIconCategory());
        node.put("iconFile", tag.getIconFile());
        node.put("backgroundCategory", tag.getBackgroundCategory());
        node.put("backgroundFile", tag.getBackgroundFile());
        node.put("backgroundPrimaryColor", tag.getBackgroundPrimaryColor());
        node.put("backgroundSecondaryColor", tag.getBackgroundSecondaryColor());
    }

    @Override
    protected GameCampaign<StellarisTag,StellarisSavegameInfo> createNewCampaignForEntry(GameCampaignEntry<StellarisTag, StellarisSavegameInfo> entry) {
        return new GameCampaign<>(
                Instant.now(),
                entry.getInfo().getTag().getName(),
                entry.getInfo().getCampaignUuid(),
                entry.getInfo().getDate(),
                entry.getInfo().getTag());
    }

    @Override
    protected GameCampaignEntry<StellarisTag, StellarisSavegameInfo> createEntry(UUID uuid, String checksum, StellarisSavegameInfo info) {
        return new GameCampaignEntry<>(
                info.getDate().toDisplayString(),
                uuid,
                info,
                checksum,
                info.getDate(),
                info.getTag());
    }

    @Override
    protected boolean needsUpdate(GameCampaignEntry<StellarisTag, StellarisSavegameInfo> stellarisCampaignEntry) {
        Path p = getPath(stellarisCampaignEntry);
        int v = 0;
        try {
            v = p.toFile().exists() ? StellarisSavegame.getVersion(p.resolve("data.zip")) : 0;
        } catch (Exception ex) {
            ErrorHandler.handleException(ex);
            return true;
        }

        return v < StellarisSavegame.VERSION;
    }

    @Override
    protected StellarisSavegameInfo loadInfo(StellarisSavegame data) throws Exception {
        return StellarisSavegameInfo.fromSavegame(data);
    }

    @Override
    protected StellarisRawSavegame loadRaw(Path p) throws Exception {
        return StellarisRawSavegame.fromFile(p);
    }

    @Override
    protected StellarisSavegame loadDataFromFile(Path p) throws Exception {
        return StellarisSavegame.fromFile(p);
    }

    @Override
    protected StellarisSavegame loadDataFromRaw(StellarisRawSavegame raw) throws Exception {
        return StellarisSavegame.fromSavegame(raw);
    }
}

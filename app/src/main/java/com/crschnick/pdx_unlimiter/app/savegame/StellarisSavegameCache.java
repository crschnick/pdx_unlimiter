package com.crschnick.pdx_unlimiter.app.savegame;

import com.crschnick.pdx_unlimiter.app.game.GameCampaign;
import com.crschnick.pdx_unlimiter.app.game.GameCampaignEntry;
import com.crschnick.pdx_unlimiter.core.data.GameDate;
import com.crschnick.pdx_unlimiter.core.data.GameDateType;
import com.crschnick.pdx_unlimiter.core.data.StellarisTag;
import com.crschnick.pdx_unlimiter.core.parser.Node;
import com.crschnick.pdx_unlimiter.core.savegame.StellarisSavegameInfo;
import com.crschnick.pdx_unlimiter.core.savegame.StellarisSavegameParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.time.Instant;
import java.util.UUID;

public class StellarisSavegameCache extends SavegameCache<
        StellarisTag,
        StellarisSavegameInfo> {
    public StellarisSavegameCache() {
        super("stellaris", "sav", GameDateType.STELLARIS, new StellarisSavegameParser());
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
    protected GameCampaign<StellarisTag, StellarisSavegameInfo> createNewCampaignForEntry(GameCampaignEntry<StellarisTag, StellarisSavegameInfo> entry) {
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
    protected StellarisSavegameInfo loadInfo(Node n) throws Exception {
        return StellarisSavegameInfo.fromSavegame(n);
    }
}

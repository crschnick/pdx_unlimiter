package com.crschnick.pdx_unlimiter.app.savegame;

import com.crschnick.pdx_unlimiter.app.game.GameCampaign;
import com.crschnick.pdx_unlimiter.app.game.GameCampaignEntry;
import com.crschnick.pdx_unlimiter.app.game.GameLocalisation;
import com.crschnick.pdx_unlimiter.core.data.Eu4Tag;
import com.crschnick.pdx_unlimiter.core.data.GameDate;
import com.crschnick.pdx_unlimiter.core.data.GameDateType;
import com.crschnick.pdx_unlimiter.core.parser.Node;
import com.crschnick.pdx_unlimiter.core.savegame.Eu4SavegameInfo;
import com.crschnick.pdx_unlimiter.core.savegame.Eu4SavegameParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public class Eu4SavegameCache extends SavegameCache<
        Eu4Tag,
        Eu4SavegameInfo> {

    public Eu4SavegameCache() {
        super("eu4", "eu4", GameDateType.EU4, new Eu4SavegameParser());
    }

    @Override
    protected GameCampaignEntry<Eu4Tag, Eu4SavegameInfo> readEntry(JsonNode node, String name, UUID uuid, String checksum, GameDate date) {
        String tag = node.required("tag").textValue();
        return new GameCampaignEntry<>(name, uuid, null, checksum, date, new Eu4Tag(tag, 0, 0, Optional.empty()));
    }

    @Override
    protected GameCampaign<Eu4Tag, Eu4SavegameInfo> readCampaign(JsonNode node, String name, UUID uuid, Instant lastPlayed, GameDate date) {
        String tag = node.required("tag").textValue();
        return new GameCampaign<>(lastPlayed, name, uuid, date, new Eu4Tag(tag, 0, 0, Optional.empty()));
    }

    @Override
    protected void writeEntry(ObjectNode node, GameCampaignEntry<Eu4Tag, Eu4SavegameInfo> e) {
        node.put("tag", e.getTag().getTag());
    }

    @Override
    protected void writeCampaign(ObjectNode node, GameCampaign<Eu4Tag, Eu4SavegameInfo> c) {
        node.put("tag", c.getTag().getTag());
    }

    @Override
    protected GameCampaign<Eu4Tag, Eu4SavegameInfo> createNewCampaignForEntry(GameCampaignEntry<Eu4Tag, Eu4SavegameInfo> entry) {
        return new GameCampaign<>(
                Instant.now(),
                GameLocalisation.getTagNameForEntry(entry, entry.getInfo().getTag()),
                entry.getInfo().getCampaignUuid(),
                entry.getInfo().getDate(),
                entry.getInfo().getTag());
    }

    @Override
    protected GameCampaignEntry<Eu4Tag, Eu4SavegameInfo> createEntry(UUID uuid, String checksum, Eu4SavegameInfo info) {
        return new GameCampaignEntry<Eu4Tag, Eu4SavegameInfo>(
                info.getDate().toDisplayString(),
                uuid,
                info,
                checksum,
                info.getDate(),
                info.getTag());
    }


    @Override
    protected Eu4SavegameInfo loadInfo(Node n) throws Exception {
        return Eu4SavegameInfo.fromSavegame(n);
    }
}

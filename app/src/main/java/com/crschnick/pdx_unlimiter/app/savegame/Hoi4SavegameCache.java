package com.crschnick.pdx_unlimiter.app.savegame;

import com.crschnick.pdx_unlimiter.app.game.GameCampaign;
import com.crschnick.pdx_unlimiter.app.game.GameCampaignEntry;
import com.crschnick.pdx_unlimiter.core.data.GameDate;
import com.crschnick.pdx_unlimiter.core.data.GameDateType;
import com.crschnick.pdx_unlimiter.core.data.Hoi4Tag;
import com.crschnick.pdx_unlimiter.core.parser.Node;
import com.crschnick.pdx_unlimiter.core.savegame.Hoi4SavegameInfo;
import com.crschnick.pdx_unlimiter.core.savegame.Hoi4SavegameParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.time.Instant;
import java.util.UUID;

public class Hoi4SavegameCache extends SavegameCache<Hoi4Tag, Hoi4SavegameInfo> {

    public Hoi4SavegameCache() {
        super("hoi4", "hoi4", GameDateType.HOI4, new Hoi4SavegameParser());
    }

    @Override
    protected GameCampaignEntry<Hoi4Tag, Hoi4SavegameInfo> readEntry(JsonNode node, String name, UUID uuid, String checksum, GameDate date) {
        Hoi4Tag tag = new Hoi4Tag(node.required("tag").textValue(), node.required("ideology").textValue());
        return new GameCampaignEntry<>(name, uuid, null, checksum, date, tag);
    }

    @Override
    protected GameCampaign<Hoi4Tag, Hoi4SavegameInfo> readCampaign(JsonNode node, String name, UUID uuid, Instant lastPlayed, GameDate date) {
        Hoi4Tag tag = new Hoi4Tag(node.required("tag").textValue(), node.required("ideology").textValue());
        return new GameCampaign<>(lastPlayed, name, uuid, date, tag);
    }

    @Override
    protected void writeEntry(ObjectNode node, GameCampaignEntry<Hoi4Tag, Hoi4SavegameInfo> entry) {


        node.put("tag", entry.getTag().getTag())
                .put("ideology", entry.getTag().getIdeology());
    }

    @Override
    protected void writeCampaign(ObjectNode node, GameCampaign<Hoi4Tag, Hoi4SavegameInfo> campaign) {
        node.put("tag", campaign.getTag().getTag())
                .put("ideology", campaign.getTag().getIdeology());
    }

    @Override
    protected GameCampaign<Hoi4Tag, Hoi4SavegameInfo> createNewCampaignForEntry(GameCampaignEntry<Hoi4Tag, Hoi4SavegameInfo> entry) {
        return new GameCampaign<>(Instant.now(),
                entry.getInfo().getTag().getTag(),
                entry.getInfo().getCampaignUuid(),
                entry.getInfo().getDate(),
                entry.getInfo().getTag());
    }

    @Override
    protected GameCampaignEntry<Hoi4Tag, Hoi4SavegameInfo> createEntry(UUID uuid, String checksum, Hoi4SavegameInfo info) {
        return new GameCampaignEntry<>(
                info.getDate().toDisplayString(), uuid,
                info, checksum, info.getDate(), info.getTag());
    }

    @Override
    protected Hoi4SavegameInfo loadInfo(Node n) throws Exception {
        return Hoi4SavegameInfo.fromSavegame(n);
    }
}

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
    protected String getDefaultEntryName(Hoi4SavegameInfo info) {
        return info.getDate().toDisplayString();
    }

    @Override
    protected String getDefaultCampaignName(GameCampaignEntry<Hoi4Tag, Hoi4SavegameInfo> latest) {
        return "Unknown";
    }

    @Override
    protected Hoi4SavegameInfo loadInfo(Node n) throws Exception {
        return Hoi4SavegameInfo.fromSavegame(n);
    }
}

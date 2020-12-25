package com.crschnick.pdx_unlimiter.app.savegame;

import com.crschnick.pdx_unlimiter.app.game.GameCampaign;
import com.crschnick.pdx_unlimiter.app.game.GameCampaignEntry;
import com.crschnick.pdx_unlimiter.core.data.Ck3Tag;
import com.crschnick.pdx_unlimiter.core.data.GameDate;
import com.crschnick.pdx_unlimiter.core.data.GameDateType;
import com.crschnick.pdx_unlimiter.core.parser.Node;
import com.crschnick.pdx_unlimiter.core.savegame.Ck3SavegameInfo;
import com.crschnick.pdx_unlimiter.core.savegame.Ck3SavegameParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Ck3SavegameCache extends SavegameCache<Ck3Tag, Ck3SavegameInfo> {

    public Ck3SavegameCache() {
        super("ck3", "ck3", GameDateType.CK3, new Ck3SavegameParser());
    }

    @Override
    protected String getDefaultEntryName(Ck3SavegameInfo info) {
        return info.getDate().toDisplayString();
    }

    @Override
    protected String getDefaultCampaignName(GameCampaignEntry<Ck3Tag, Ck3SavegameInfo> latest) {
        return latest.getInfo().getTag().getPrimaryTitle().getName();
    }

    @Override
    protected Ck3SavegameInfo loadInfo(Node n) throws Exception {
        return Ck3SavegameInfo.fromSavegame(n);
    }
}

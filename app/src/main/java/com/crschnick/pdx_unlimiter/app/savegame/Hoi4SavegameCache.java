package com.crschnick.pdx_unlimiter.app.savegame;

import com.crschnick.pdx_unlimiter.core.data.GameDateType;
import com.crschnick.pdx_unlimiter.core.data.Hoi4Tag;
import com.crschnick.pdx_unlimiter.core.savegame.Hoi4SavegameInfo;
import com.crschnick.pdx_unlimiter.core.savegame.Hoi4SavegameParser;

public class Hoi4SavegameCache extends SavegameCache<Hoi4Tag, Hoi4SavegameInfo> {

    public Hoi4SavegameCache() {
        super("hoi4", "hoi4", GameDateType.HOI4, new Hoi4SavegameParser());
    }

    @Override
    protected String getDefaultEntryName(Hoi4SavegameInfo info) {
        return info.getDate().toDisplayString();
    }

    @Override
    protected String getDefaultCampaignName(SavegameEntry<Hoi4Tag, Hoi4SavegameInfo> latest) {
        return "Unknown";
    }
}

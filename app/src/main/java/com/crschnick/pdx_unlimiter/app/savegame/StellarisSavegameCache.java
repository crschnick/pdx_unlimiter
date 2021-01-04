package com.crschnick.pdx_unlimiter.app.savegame;

import com.crschnick.pdx_unlimiter.app.game.GameCampaignEntry;
import com.crschnick.pdx_unlimiter.core.data.GameDateType;
import com.crschnick.pdx_unlimiter.core.data.StellarisTag;
import com.crschnick.pdx_unlimiter.core.parser.Node;
import com.crschnick.pdx_unlimiter.core.savegame.StellarisSavegameInfo;
import com.crschnick.pdx_unlimiter.core.savegame.StellarisSavegameParser;

public class StellarisSavegameCache extends SavegameCache<
        StellarisTag,
        StellarisSavegameInfo> {
    public StellarisSavegameCache() {
        super("stellaris", "sav", GameDateType.STELLARIS, new StellarisSavegameParser());
    }

    @Override
    protected String getDefaultEntryName(StellarisSavegameInfo info) {
        return info.getDate().toDisplayString();
    }

    @Override
    protected String getDefaultCampaignName(GameCampaignEntry<StellarisTag, StellarisSavegameInfo> latest) {
        return latest.getInfo().getTag().getName();
    }

    @Override
    protected StellarisSavegameInfo loadInfo(boolean melted, Node n) throws Exception {
        return StellarisSavegameInfo.fromSavegame(n);
    }
}

package com.crschnick.pdx_unlimiter.app.savegame;

import com.crschnick.pdx_unlimiter.app.installation.IntegrityManager;
import com.crschnick.pdx_unlimiter.core.info.GameDateType;
import com.crschnick.pdx_unlimiter.core.info.stellaris.StellarisSavegameParser;
import com.crschnick.pdx_unlimiter.core.info.stellaris.StellarisTag;
import com.crschnick.pdx_unlimiter.core.savegame.StellarisSavegameInfo;

public class StellarisSavegameCache extends SavegameCache<
        StellarisTag,
        StellarisSavegameInfo> {
    public StellarisSavegameCache() {
        super("stellaris",
                "sav",
                GameDateType.STELLARIS,
                new StellarisSavegameParser(),
                StellarisSavegameInfo.class,
                IntegrityManager.getInstance().getStellarisChecksum());
    }

    @Override
    protected String getDefaultEntryName(StellarisSavegameInfo info) {
        return info.getDate().toDisplayString();
    }

    @Override
    protected String getDefaultCampaignName(SavegameEntry<StellarisTag, StellarisSavegameInfo> latest) {
        return latest.getInfo().getTag().getName();
    }
}

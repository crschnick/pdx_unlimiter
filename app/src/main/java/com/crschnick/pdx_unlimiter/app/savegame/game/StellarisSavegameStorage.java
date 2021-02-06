package com.crschnick.pdx_unlimiter.app.savegame.game;

import com.crschnick.pdx_unlimiter.app.core.IntegrityManager;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameEntry;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameStorage;
import com.crschnick.pdx_unlimiter.core.info.GameDateType;
import com.crschnick.pdx_unlimiter.core.info.stellaris.StellarisSavegameInfo;
import com.crschnick.pdx_unlimiter.core.info.stellaris.StellarisTag;
import com.crschnick.pdx_unlimiter.core.savegame.StellarisSavegameParser;

public class StellarisSavegameStorage extends SavegameStorage<
        StellarisTag,
        StellarisSavegameInfo> {
    public StellarisSavegameStorage() {
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

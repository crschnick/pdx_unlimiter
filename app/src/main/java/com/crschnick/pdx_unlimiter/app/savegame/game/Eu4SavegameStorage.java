package com.crschnick.pdx_unlimiter.app.savegame.game;

import com.crschnick.pdx_unlimiter.app.core.IntegrityManager;
import com.crschnick.pdx_unlimiter.app.installation.GameLocalisation;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameEntry;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameStorage;
import com.crschnick.pdx_unlimiter.core.info.GameDateType;
import com.crschnick.pdx_unlimiter.core.info.eu4.Eu4SavegameInfo;
import com.crschnick.pdx_unlimiter.core.info.eu4.Eu4Tag;
import com.crschnick.pdx_unlimiter.core.savegame.Eu4SavegameParser;

public class Eu4SavegameStorage extends SavegameStorage<
        Eu4Tag,
        Eu4SavegameInfo> {

    public Eu4SavegameStorage() {
        super("eu4",
                "eu4",
                GameDateType.EU4,
                new Eu4SavegameParser(),
                Eu4SavegameInfo.class,
                IntegrityManager.getInstance().getEu4Checksum());
    }

    @Override
    protected String getDefaultEntryName(Eu4SavegameInfo info) {
        return info.getDate().toDisplayString();
    }

    @Override
    protected String getDefaultCampaignName(Eu4SavegameInfo info) {
        return GameLocalisation.getTagNameForEntry(info, info.getTag());
    }
}

package com.crschnick.pdx_unlimiter.app.savegame;

import com.crschnick.pdx_unlimiter.app.game.GameLocalisation;
import com.crschnick.pdx_unlimiter.app.installation.IntegrityManager;
import com.crschnick.pdx_unlimiter.core.info.GameDateType;
import com.crschnick.pdx_unlimiter.core.info.eu4.Eu4SavegameInfo;
import com.crschnick.pdx_unlimiter.core.info.eu4.Eu4Tag;
import com.crschnick.pdx_unlimiter.core.savegame.Eu4SavegameParser;

public class Eu4SavegameCache extends SavegameCache<
        Eu4Tag,
        Eu4SavegameInfo> {

    public Eu4SavegameCache() {
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
    protected String getDefaultCampaignName(SavegameEntry<Eu4Tag, Eu4SavegameInfo> latest) {
        return GameLocalisation.getTagNameForEntry(latest.getInfo(), latest.getInfo().getTag());
    }
}

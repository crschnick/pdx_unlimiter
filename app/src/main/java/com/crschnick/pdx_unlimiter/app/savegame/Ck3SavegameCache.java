package com.crschnick.pdx_unlimiter.app.savegame;

import com.crschnick.pdx_unlimiter.app.installation.IntegrityManager;
import com.crschnick.pdx_unlimiter.core.info.ck3.Ck3Tag;
import com.crschnick.pdx_unlimiter.core.info.GameDateType;
import com.crschnick.pdx_unlimiter.core.info.ck3.Ck3SavegameInfo;
import com.crschnick.pdx_unlimiter.core.savegame.Ck3SavegameParser;

public class Ck3SavegameCache extends SavegameCache<Ck3Tag, Ck3SavegameInfo> {

    public Ck3SavegameCache() {
        super("ck3", "ck3", GameDateType.CK3,
                new Ck3SavegameParser(), Ck3SavegameInfo.class, IntegrityManager.getInstance().getCk3Checksum());
    }

    @Override
    protected String getDefaultEntryName(Ck3SavegameInfo info) {
        return info.getDate().toDisplayString();
    }

    @Override
    protected String getDefaultCampaignName(SavegameEntry<Ck3Tag, Ck3SavegameInfo> latest) {
        return latest.getInfo().getTag().getPrimaryTitle().getName();
    }
}

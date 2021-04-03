package com.crschnick.pdx_unlimiter.app.savegame.game;

import com.crschnick.pdx_unlimiter.app.core.IntegrityManager;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameStorage;
import com.crschnick.pdx_unlimiter.core.info.GameDateType;
import com.crschnick.pdx_unlimiter.core.info.ck3.Ck3SavegameInfo;
import com.crschnick.pdx_unlimiter.core.info.ck3.Ck3Tag;
import com.crschnick.pdx_unlimiter.core.savegame.Ck3SavegameParser;

public class Ck3SavegameStorage extends SavegameStorage<Ck3Tag, Ck3SavegameInfo> {

    public Ck3SavegameStorage() {
        super("ck3", "ck3", GameDateType.CK3,
                new Ck3SavegameParser(), Ck3SavegameInfo.class, IntegrityManager.getInstance().getCk3Checksum());
    }

    @Override
    protected String getDefaultEntryName(Ck3SavegameInfo info) {
        return info.getDate().toDisplayString();
    }

    @Override
    protected String getDefaultCampaignName(Ck3SavegameInfo info) {
        if (info.isMultiplayer()) {
            return "Multiplayer";
        }
        if (info.isObserver()) {
            return "Observer";
        }
        return info.getTag().getName();
    }
}

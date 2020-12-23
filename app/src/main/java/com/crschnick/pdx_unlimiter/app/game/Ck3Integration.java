package com.crschnick.pdx_unlimiter.app.game;

import com.crschnick.pdx_unlimiter.app.gui.Ck3GuiFactory;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameCache;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameWatcher;
import com.crschnick.pdx_unlimiter.core.data.Ck3Tag;
import com.crschnick.pdx_unlimiter.core.savegame.Ck3SavegameInfo;

public class Ck3Integration extends GameIntegration<Ck3Tag, Ck3SavegameInfo> {
    @Override
    public String getName() {
        return "Crusader Kings III";
    }

    @Override
    public GameInstallation getInstallation() {
        return GameInstallation.CK3;
    }

    @Override
    public SavegameWatcher getSavegameWatcher() {
        return SavegameWatcher.CK3;
    }

    @Override
    public Ck3GuiFactory getGuiFactory() {
        return new Ck3GuiFactory();
    }

    @Override
    public SavegameCache<Ck3Tag, Ck3SavegameInfo> getSavegameCache() {
        return SavegameCache.CK3;
    }
}

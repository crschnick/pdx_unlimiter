package com.crschnick.pdx_unlimiter.app.game;

import com.crschnick.pdx_unlimiter.app.gui.GameGuiFactory;
import com.crschnick.pdx_unlimiter.app.gui.StellarisGuiFactory;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameCache;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameWatcher;
import com.crschnick.pdx_unlimiter.core.info.stellaris.StellarisTag;
import com.crschnick.pdx_unlimiter.core.savegame.StellarisSavegameInfo;

public class StellarisIntegration extends GameIntegration<StellarisTag, StellarisSavegameInfo> {

    @Override
    public String getName() {
        return "Stellaris";
    }

    @Override
    public GameInstallation getInstallation() {
        return GameInstallation.STELLARIS;
    }

    @Override
    public SavegameWatcher getSavegameWatcher() {
        return SavegameWatcher.STELLARIS;
    }

    @Override
    public GameGuiFactory<StellarisTag, StellarisSavegameInfo> getGuiFactory() {
        return new StellarisGuiFactory();
    }

    @Override
    public SavegameCache<StellarisTag, StellarisSavegameInfo> getSavegameCache() {
        return SavegameCache.STELLARIS;
    }
}

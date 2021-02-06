package com.crschnick.pdx_unlimiter.app.installation.game;

import com.crschnick.pdx_unlimiter.app.gui.game.GameGuiFactory;
import com.crschnick.pdx_unlimiter.app.gui.game.StellarisGuiFactory;
import com.crschnick.pdx_unlimiter.app.installation.GameInstallation;
import com.crschnick.pdx_unlimiter.app.installation.GameIntegration;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameStorage;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameWatcher;
import com.crschnick.pdx_unlimiter.core.info.stellaris.StellarisSavegameInfo;
import com.crschnick.pdx_unlimiter.core.info.stellaris.StellarisTag;

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
    public SavegameStorage<StellarisTag, StellarisSavegameInfo> getSavegameCache() {
        return SavegameStorage.STELLARIS;
    }
}

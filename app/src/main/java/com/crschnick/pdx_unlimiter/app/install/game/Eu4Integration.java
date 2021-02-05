package com.crschnick.pdx_unlimiter.app.install.game;

import com.crschnick.pdx_unlimiter.app.gui.game.Eu4GuiFactory;
import com.crschnick.pdx_unlimiter.app.gui.game.GameGuiFactory;
import com.crschnick.pdx_unlimiter.app.install.GameInstallation;
import com.crschnick.pdx_unlimiter.app.install.GameIntegration;
import com.crschnick.pdx_unlimiter.app.savegame.game.Eu4SavegameStorage;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameStorage;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameWatcher;
import com.crschnick.pdx_unlimiter.core.info.eu4.Eu4SavegameInfo;
import com.crschnick.pdx_unlimiter.core.info.eu4.Eu4Tag;

public class Eu4Integration extends GameIntegration<Eu4Tag, Eu4SavegameInfo> {

    @Override
    public GameGuiFactory<Eu4Tag, Eu4SavegameInfo> getGuiFactory() {
        return new Eu4GuiFactory();
    }

    @Override
    public Eu4SavegameStorage getSavegameCache() {
        return SavegameStorage.EU4;
    }

    @Override
    public String getName() {
        return "Europa Universalis IV";
    }

    @Override
    public GameInstallation getInstallation() {
        return GameInstallation.EU4;
    }

    @Override
    public SavegameWatcher getSavegameWatcher() {
        return SavegameWatcher.EU4;
    }
}

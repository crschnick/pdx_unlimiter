package com.crschnick.pdx_unlimiter.app.game;

import com.crschnick.pdx_unlimiter.app.gui.Eu4GuiFactory;
import com.crschnick.pdx_unlimiter.app.gui.GameGuiFactory;
import com.crschnick.pdx_unlimiter.app.savegame.Eu4SavegameCache;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameCache;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameWatcher;
import com.crschnick.pdx_unlimiter.core.data.Eu4Tag;
import com.crschnick.pdx_unlimiter.core.savegame.Eu4SavegameInfo;

public class Eu4Integration extends GameIntegration<Eu4Tag, Eu4SavegameInfo> {

    @Override
    public GameGuiFactory<Eu4Tag, Eu4SavegameInfo> getGuiFactory() {
        return new Eu4GuiFactory();
    }

    @Override
    public Eu4SavegameCache getSavegameCache() {
        return SavegameCache.EU4;
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

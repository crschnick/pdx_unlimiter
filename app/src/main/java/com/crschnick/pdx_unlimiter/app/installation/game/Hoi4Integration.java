package com.crschnick.pdx_unlimiter.app.installation.game;

import com.crschnick.pdx_unlimiter.app.gui.game.Hoi4GuiFactory;
import com.crschnick.pdx_unlimiter.app.installation.GameInstallation;
import com.crschnick.pdx_unlimiter.app.installation.GameIntegration;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameStorage;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameWatcher;
import com.crschnick.pdx_unlimiter.app.savegame.game.Hoi4SavegameStorage;
import com.crschnick.pdx_unlimiter.core.info.hoi4.Hoi4SavegameInfo;
import com.crschnick.pdx_unlimiter.core.info.hoi4.Hoi4Tag;

public class Hoi4Integration extends GameIntegration<Hoi4Tag, Hoi4SavegameInfo> {
    @Override
    public String getName() {
        return "Hearts of Iron IV";
    }

    @Override
    public GameInstallation getInstallation() {
        return GameInstallation.HOI4;
    }

    @Override
    public SavegameWatcher getSavegameWatcher() {
        return SavegameWatcher.HOI4;
    }

    @Override
    public Hoi4GuiFactory getGuiFactory() {
        return new Hoi4GuiFactory();
    }

    @Override
    public Hoi4SavegameStorage getSavegameCache() {
        return SavegameStorage.HOI4;
    }
}

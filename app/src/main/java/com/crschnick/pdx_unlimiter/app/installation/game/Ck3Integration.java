package com.crschnick.pdx_unlimiter.app.installation.game;

import com.crschnick.pdx_unlimiter.app.gui.game.Ck3GuiFactory;
import com.crschnick.pdx_unlimiter.app.installation.GameInstallation;
import com.crschnick.pdx_unlimiter.app.installation.GameIntegration;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameStorage;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameWatcher;
import com.crschnick.pdx_unlimiter.core.info.ck3.Ck3SavegameInfo;
import com.crschnick.pdx_unlimiter.core.info.ck3.Ck3Tag;

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
    public SavegameStorage<Ck3Tag, Ck3SavegameInfo> getSavegameCache() {
        return SavegameStorage.CK3;
    }
}

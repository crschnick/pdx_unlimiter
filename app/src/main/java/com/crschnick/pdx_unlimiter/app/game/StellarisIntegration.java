package com.crschnick.pdx_unlimiter.app.game;

import com.crschnick.pdx_unlimiter.app.achievement.AchievementManager;
import com.crschnick.pdx_unlimiter.app.gui.GameGuiFactory;
import com.crschnick.pdx_unlimiter.app.gui.StellarisGuiFactory;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameCache;
import com.crschnick.pdx_unlimiter.core.data.StellarisTag;
import com.crschnick.pdx_unlimiter.core.savegame.RawSavegame;
import com.crschnick.pdx_unlimiter.core.savegame.Savegame;
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
    public AchievementManager getAchievementManager() {
        return null;
    }

    @Override
    public GameGuiFactory<StellarisTag, StellarisSavegameInfo> getGuiFactory() {
        return new StellarisGuiFactory();
    }

    @Override
    public SavegameCache<? extends RawSavegame, ? extends Savegame, StellarisTag, StellarisSavegameInfo> getSavegameCache() {
        return SavegameCache.STELLARIS_CACHE;
    }
}

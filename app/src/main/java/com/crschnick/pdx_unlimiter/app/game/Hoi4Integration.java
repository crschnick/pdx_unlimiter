package com.crschnick.pdx_unlimiter.app.game;

import com.crschnick.pdx_unlimiter.app.achievement.AchievementManager;
import com.crschnick.pdx_unlimiter.app.gui.Hoi4GuiFactory;
import com.crschnick.pdx_unlimiter.app.savegame.Hoi4SavegameCache;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameCache;
import com.crschnick.pdx_unlimiter.core.data.Hoi4Tag;
import com.crschnick.pdx_unlimiter.core.savegame.Hoi4SavegameInfo;

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
    public AchievementManager getAchievementManager() {
        return AchievementManager.HOI4;
    }

    @Override
    public boolean isVersionCompatible(GameCampaignEntry<Hoi4Tag, Hoi4SavegameInfo> entry) {
        return true;
    }

    @Override
    public Hoi4GuiFactory getGuiFactory() {
        return new Hoi4GuiFactory();
    }

    @Override
    public Hoi4SavegameCache getSavegameCache() {
        return SavegameCache.HOI4;
    }
}

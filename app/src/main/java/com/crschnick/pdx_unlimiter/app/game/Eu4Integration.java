package com.crschnick.pdx_unlimiter.app.game;

import com.crschnick.pdx_unlimiter.app.achievement.AchievementManager;
import com.crschnick.pdx_unlimiter.app.gui.Eu4GuiFactory;
import com.crschnick.pdx_unlimiter.app.gui.GameGuiFactory;
import com.crschnick.pdx_unlimiter.app.savegame.Eu4SavegameCache;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameCache;
import com.crschnick.pdx_unlimiter.app.util.JsonHelper;
import com.crschnick.pdx_unlimiter.eu4.data.Eu4Tag;
import com.crschnick.pdx_unlimiter.eu4.data.GameVersion;
import com.crschnick.pdx_unlimiter.eu4.savegame.Eu4Savegame;
import com.crschnick.pdx_unlimiter.eu4.savegame.Eu4SavegameInfo;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Eu4Integration extends GameIntegration<Eu4Tag, Eu4SavegameInfo> {

    @Override
    public GameGuiFactory<Eu4Tag, Eu4SavegameInfo> getGuiFactory() {
        return new Eu4GuiFactory();
    }

    @Override
    public Eu4SavegameCache getSavegameCache() {
        return SavegameCache.EU4_CACHE;
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
    public AchievementManager getAchievementManager() {
        return AchievementManager.EU4;
    }
}

package com.crschnick.pdx_unlimiter.app.game;

import com.crschnick.pdx_unlimiter.app.achievement.AchievementManager;
import com.crschnick.pdx_unlimiter.app.gui.Eu4GuiFactory;
import com.crschnick.pdx_unlimiter.app.gui.GameGuiFactory;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameCache;
import com.crschnick.pdx_unlimiter.app.util.JsonHelper;
import com.crschnick.pdx_unlimiter.eu4.data.GameVersion;
import com.crschnick.pdx_unlimiter.eu4.savegame.Eu4Savegame;
import com.crschnick.pdx_unlimiter.eu4.savegame.Eu4SavegameInfo;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Eu4Integration extends GameIntegration<Eu4CampaignEntry, Eu4Campaign> {

    @Override
    public GameGuiFactory<Eu4CampaignEntry, Eu4Campaign> getGuiFactory() {
        return new Eu4GuiFactory();
    }

    @Override
    public SavegameCache<Eu4Savegame, Eu4SavegameInfo, Eu4CampaignEntry, Eu4Campaign> getSavegameCache() {
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


    protected void writeLaunchConfig(Eu4CampaignEntry entry, Path path) throws IOException {
        var out = Files.newOutputStream(
                getInstallation().getUserPath().resolve("continue_game.json"));
        ObjectNode n = JsonNodeFactory.instance.objectNode()
                .put("title", getSavegameCache().getCampaign(entry).getName())
                .put("desc", entry.getName())
                .put("date", getSavegameCache().getCampaign(entry).getLastPlayed().toString())
                .put("filename", getInstallation().getUserPath().relativize(path).toString()
                        .replace('\\', '/'));
        JsonHelper.write(n, out);
    }
}

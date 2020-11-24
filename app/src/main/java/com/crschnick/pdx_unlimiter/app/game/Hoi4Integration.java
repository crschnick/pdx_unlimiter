package com.crschnick.pdx_unlimiter.app.game;

import com.crschnick.pdx_unlimiter.app.achievement.AchievementManager;
import com.crschnick.pdx_unlimiter.app.gui.GameGuiFactory;
import com.crschnick.pdx_unlimiter.app.gui.Hoi4GuiFactory;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameCache;
import com.crschnick.pdx_unlimiter.app.util.JsonHelper;
import com.crschnick.pdx_unlimiter.eu4.savegame.Hoi4Savegame;
import com.crschnick.pdx_unlimiter.eu4.savegame.Hoi4SavegameInfo;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Hoi4Integration extends GameIntegration<Hoi4CampaignEntry, Hoi4Campaign> {
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
    public boolean isVersionCompatible(Hoi4CampaignEntry entry) {
        return true;
    }

    @Override
    public GameGuiFactory<Hoi4CampaignEntry, Hoi4Campaign> getGuiFactory() {
        return new Hoi4GuiFactory();
    }

    @Override
    public SavegameCache<Hoi4Savegame, Hoi4SavegameInfo, Hoi4CampaignEntry, Hoi4Campaign> getSavegameCache() {
        return SavegameCache.HOI4_CACHE;
    }

    protected void writeLaunchConfig(Hoi4CampaignEntry entry, Path path) throws IOException {
        var out = Files.newOutputStream(
                getInstallation().getUserPath().resolve("continue_game.json"));
        SimpleDateFormat d = new SimpleDateFormat("E MMM dd HH:mm:ss yyyy");
        ObjectNode n = JsonNodeFactory.instance.objectNode()
                .put("title", getSavegameCache().getCampaign(entry).getName() + " " + entry.getName())
                .put("desc", "")
                .put("date", d.format(new Date(getSavegameCache().getCampaign(entry).getLastPlayed().toEpochMilli())) + "\n")
                .put("filename", getInstallation().getSavegamesPath().relativize(path).toString())
                .put("is_remote", false);
        JsonHelper.write(n, out);
    }
}

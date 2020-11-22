package com.crschnick.pdx_unlimiter.app.game;

import com.crschnick.pdx_unlimiter.app.gui.Eu4GuiFactory;
import com.crschnick.pdx_unlimiter.app.gui.GameGuiFactory;
import com.crschnick.pdx_unlimiter.app.installation.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameCache;
import com.crschnick.pdx_unlimiter.app.util.JsonHelper;
import com.crschnick.pdx_unlimiter.eu4.savegame.SavegameInfo;
import com.crschnick.pdx_unlimiter.eu4.parser.GameVersion;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Optional;

public class Eu4Integration extends GameIntegration<Eu4CampaignEntry,Eu4Campaign> {

    private static boolean areCompatible(GameVersion gameVersion, GameVersion saveVersion) {
        return gameVersion.getFirst() == saveVersion.getFirst() && gameVersion.getSecond() == saveVersion.getSecond();
    }

    @Override
    public boolean isVersionCompatible(Eu4CampaignEntry entry) {
        return areCompatible(GameInstallation.EU4.getVersion(), entry.getInfo().getVersion());
    }

    @Override
    public GameGuiFactory<Eu4CampaignEntry, Eu4Campaign> getGuiFactory() {
        return new Eu4GuiFactory();
    }

    @Override
    public SavegameCache<? extends SavegameInfo, Eu4CampaignEntry, Eu4Campaign> getSavegameCache() {
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

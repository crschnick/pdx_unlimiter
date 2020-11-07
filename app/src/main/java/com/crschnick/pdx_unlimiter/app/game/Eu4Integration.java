package com.crschnick.pdx_unlimiter.app.game;

import com.crschnick.pdx_unlimiter.app.gui.Eu4GuiFactory;
import com.crschnick.pdx_unlimiter.app.gui.GameGuiFactory;
import com.crschnick.pdx_unlimiter.app.installation.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameCache;
import com.crschnick.pdx_unlimiter.app.util.JsonHelper;
import com.crschnick.pdx_unlimiter.eu4.Savegame;
import com.crschnick.pdx_unlimiter.eu4.SavegameInfo;
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
    public boolean isVersionCompatibe(Eu4CampaignEntry entry) {
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
    public void launchCampaignEntry() {
        if (getSelectedCampaign() == null || getSelectedEntry() == null) {
            return;
        }

        Optional<Path> p = SavegameCache.EU4_CACHE.exportSavegame(getSelectedEntry(),
                GameInstallation.EU4.getSaveDirectory().resolve(SavegameCache.EU4_CACHE.getFileName(getSelectedEntry())));
        if (p.isPresent()) {
            try {
                writeLaunchConfig(this.getSelectedEntry(), p.get());
            } catch (IOException ioException) {
                ErrorHandler.handleException(ioException);
                return;
            }
        }
        GameInstallation.EU4.start();
        getSelectedCampaign().lastPlayedProperty().setValue(Instant.now());
    }


    private void writeLaunchConfig(Eu4CampaignEntry entry, Path path) throws IOException {
        var out = Files.newOutputStream(
                GameInstallation.EU4.getUserDirectory().resolve("continue_game.json"));
        ObjectNode n = JsonNodeFactory.instance.objectNode()
                .put("title", SavegameCache.EU4_CACHE.getCampaign(entry).getName())
                .put("desc", entry.getName())
                .put("date", SavegameCache.EU4_CACHE.getCampaign(entry).getLastPlayed().toString())
                .put("filename", GameInstallation.EU4.getUserDirectory().relativize(path).toString()
                        .replace('\\', '/'));
        JsonHelper.write(n, out);
    }
}

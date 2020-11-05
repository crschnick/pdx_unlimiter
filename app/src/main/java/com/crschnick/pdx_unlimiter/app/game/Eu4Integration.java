package com.crschnick.pdx_unlimiter.app.game;

import com.crschnick.pdx_unlimiter.app.installation.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameCache;
import com.crschnick.pdx_unlimiter.app.util.JsonHelper;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

public class Eu4Integration extends GameIntegration<Eu4CampaignEntry,Eu4Campaign> {

    @Override
    public void launchCampaignEntry() {
        if (getSelectedCampaign().isEmpty() || getSelectedEntry().isEmpty()) {
            return;
        }

        Optional<Path> p = SavegameCache.EU4_CACHE.exportSavegame(getSelectedEntry().get(),
                GameInstallation.EU4.getSaveDirectory().resolve(SavegameCache.EU4_CACHE.getFileName(getSelectedEntry().get())));
        if (p.isPresent()) {
            try {
                writeLaunchConfig(getSelectedEntry().get(), p.get());
            } catch (IOException ioException) {
                ErrorHandler.handleException(ioException);
                return;
            }
        }
        GameInstallation.EU4.start();
        getSelectedCampaign().get().lastPlayedProperty().setValue(Timestamp.from(Instant.now()));
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

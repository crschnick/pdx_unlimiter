package com.crschnick.pdx_unlimiter.app.savegame;

import com.crschnick.pdx_unlimiter.app.game.GameInstallation;
import com.crschnick.pdx_unlimiter.app.installation.ErrorHandler;
import com.crschnick.pdx_unlimiter.eu4.parser.Eu4Savegame;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Optional;

public class Eu4SavegameImporter {

    private static Optional<String> getCampaignName(Path p) {
        try {
            boolean ironman = Eu4Savegame.isIronman(p);
            if (ironman) {
                String s = p.getFileName().toString().replace("_Backup", "");
                return Optional.of(s.substring(0, s.length() - 4));
            }
        } catch (IOException e) {
        }
        return Optional.empty();
    }

    public static void importLatestSavegame() {
        if (!Files.exists(GameInstallation.EU4.getSaveDirectory())) {
            return;
        }

        try {
            Optional<Path> latest = Files.list(GameInstallation.EU4.getSaveDirectory())
                    .sorted(Comparator.comparingLong(p -> p.toFile().lastModified()))
                    .sorted(Comparator.reverseOrder())
                    .findFirst();
            latest.ifPresent(p -> {
                new Thread(() -> SavegameCache.EU4_CACHE.importSavegame(getCampaignName(p), p)).start();
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void importAllSavegames() {
        try {
            Files.list(GameInstallation.EU4.getSaveDirectory())
                    .filter(p -> !Files.isDirectory(p))
                    .forEach(p -> {
                        new Thread(() -> SavegameCache.EU4_CACHE.importSavegame(getCampaignName(p), p)).start();
                    });
        } catch (IOException e) {
            ErrorHandler.handleException(e);
        }
    }
}

package com.crschnick.pdx_unlimiter.app.savegame;

import com.crschnick.pdx_unlimiter.app.game.GameInstallation;
import com.crschnick.pdx_unlimiter.eu4.parser.Eu4Savegame;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Optional;

public class FileImporter {

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

    public static boolean importFile(Path p) {
        String name = p.getFileName().toString();
        if (name.endsWith(".eu4")) {
            new Thread(() -> SavegameCache.EU4_CACHE.importSavegame(p)).start();
            return true;
        }

        return false;
    }
}

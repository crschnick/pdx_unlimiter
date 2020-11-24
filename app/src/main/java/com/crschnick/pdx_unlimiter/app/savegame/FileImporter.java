package com.crschnick.pdx_unlimiter.app.savegame;

import com.crschnick.pdx_unlimiter.app.PdxuApp;
import com.crschnick.pdx_unlimiter.app.game.GameIntegration;
import com.crschnick.pdx_unlimiter.eu4.savegame.RawSavegameVisitor;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public class FileImporter {

    public static boolean importFiles(List<File> files) {
        boolean success = false;
        for (File f : files) {
            if (importFile(f.toPath())) {
                success = true;
            }
            if (!PdxuApp.getApp().isRunning()) {
                return true;
            }
        }
        return success;
    }

    public static boolean importFile(Path p) {
        final boolean[] toReturn = {false};
        new Thread(() -> {
            RawSavegameVisitor.vist(p, new RawSavegameVisitor() {
                @Override
                public void visitEu4(Path file) {
                    SavegameCache.EU4_CACHE.importSavegame(p);
                    toReturn[0] = true;
                }

                @Override
                public void visitHoi4(Path file) {
                    SavegameCache.HOI4_CACHE.importSavegame(p);
                    toReturn[0] = true;
                }
            });
        }).start();

        return toReturn[0];
    }

    public static void importLatestSavegame() {
        var savegames = GameIntegration.current().getInstallation().getSavegames();
        if (savegames.size() == 0) {
            return;
        }

        importFile(savegames.get(0));
    }
}

package com.crschnick.pdx_unlimiter.app.savegame_mgr;

import com.crschnick.pdx_unlimiter.app.SavegameManagerApp;
import com.crschnick.pdx_unlimiter.app.installation.Installation;
import javafx.beans.property.BooleanProperty;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Consumer;

public class Eu4SavegameImporter {

    public static void importLatestSavegame() {
        Arrays.stream(Installation.EU4.get().getSaveDirectory().toFile().listFiles())
                .sorted(Comparator.comparingLong(f -> f.lastModified()))
                .sorted(Comparator.reverseOrder())
                .findFirst().ifPresent(f -> {
            SavegameCache.EU4_CACHE.importSavegame(f.toPath());
        });

    }

    public static void importAllSavegames() {
        new Thread(() -> {
            Eu4SavegameImporter.importAllSavegames(Installation.EU4.get().getSaveDirectory(), (p) -> {
                SavegameCache.EU4_CACHE.importSavegame(p);
            }, SavegameManagerApp.getAPP().runningProperty());
        }).start();

    }

    private static void importAllSavegames(Path directory, Consumer<Path> consumer, BooleanProperty running) {
        Arrays.stream(directory.toFile().listFiles()).filter(f -> !f.isDirectory()).forEach((f) -> {
            if (!running.get()) {
                return;
            }
            consumer.accept(f.toPath());
        });
    }
}

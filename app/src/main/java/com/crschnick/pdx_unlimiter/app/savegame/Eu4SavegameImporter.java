package com.crschnick.pdx_unlimiter.app.savegame;

import com.crschnick.pdx_unlimiter.app.PdxuApp;
import com.crschnick.pdx_unlimiter.app.game.GameInstallation;
import com.crschnick.pdx_unlimiter.eu4.parser.Eu4Savegame;
import javafx.beans.property.BooleanProperty;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Consumer;

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
        try {
            Optional<Path> latest = Files.list(GameInstallation.EU4.getSaveDirectory())
                    .sorted(Comparator.comparingLong(p -> p.toFile().lastModified()))
                    .sorted(Comparator.reverseOrder())
                    .findFirst();
            latest.ifPresent(p -> {
                SavegameCache.EU4_CACHE.importSavegame(getCampaignName(p), p);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void importAllSavegames() {
        new Thread(() -> {
            Eu4SavegameImporter.importAllSavegames(GameInstallation.EU4.getSaveDirectory(), (p) -> {
                SavegameCache.EU4_CACHE.importSavegame(getCampaignName(p), p);
            }, PdxuApp.getApp().runningProperty());
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

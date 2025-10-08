package com.crschnick.pdxu.app.util;

import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.app.prefs.AppPrefs;
import org.apache.commons.lang3.SystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class IronyHelper {

    public static Optional<Path> getIronyDefaultInstallPath() {
        if (!SystemUtils.IS_OS_WINDOWS) {
            return Optional.empty();
        }

        var dir = Path.of(System.getenv("LOCALAPPDATA"))
                .resolve("Programs").resolve("Irony Mod Manager");
        if (Files.exists(dir)) {
            return Optional.of(dir);
        } else {
            return Optional.empty();
        }
    }

    public static void launchEntry(Game game, boolean continueGame) throws IOException {

        var exe = AppPrefs.get().ironyDirectory().getValue().resolve("IronyModManager.exe");
        new ProcessBuilder("cmd", "/C", exe.toString(), "-g", game.getTranslatedAbbreviation(), continueGame ? "-r" : "").start();
    }
}

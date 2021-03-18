package com.crschnick.pdx_unlimiter.app.util.integration;

import com.crschnick.pdx_unlimiter.app.core.settings.Settings;
import com.crschnick.pdx_unlimiter.app.installation.Game;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class IronyHelper {

    public static Optional<Path> getIronyDefaultInstallPath() {
        var dir = Path.of(System.getenv("LOCALAPPDATA"))
                .resolve("Programs").resolve("Irony Mod Manager");
        if (Files.exists(dir)) {
            return Optional.of(dir);
        } else {
            return Optional.empty();
        }
    }

    public static void launchEntry(Game game, boolean continueGame) throws IOException {
        var exe = Settings.getInstance().ironyDir.getValue().resolve("IronyModManager.exe");
        new ProcessBuilder(exe.toString(), "-g", game.getAbbreviation(), continueGame ? "-r" : "").start();
    }
}

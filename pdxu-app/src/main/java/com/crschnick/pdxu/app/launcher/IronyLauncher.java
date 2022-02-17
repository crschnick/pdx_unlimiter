package com.crschnick.pdxu.app.launcher;

import com.crschnick.pdxu.app.core.settings.Settings;
import com.crschnick.pdxu.app.installation.Game;
import org.apache.commons.lang3.SystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class IronyLauncher extends SupportedLauncher {

    private static Optional<Path> getIronyDefaultInstallPath() {
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
        var exe = Settings.getInstance().ironyDir.getValue().resolve("IronyModManager.exe");
        new ProcessBuilder(exe.toString(), "-g", game.getTranslatedAbbreviation(), continueGame ? "-r" : "").start();
    }

    @Override
    public boolean isSupported(Game game) {
        var exe = Settings.getInstance().ironyDir.getValue().resolve("IronyModManager.exe");
        return Files.exists(exe);
    }

    @Override
    public void start(Game game) throws IOException {
        var exe = Settings.getInstance().ironyDir.getValue().resolve("IronyModManager.exe");
        var continueGame = true;
        new ProcessBuilder(exe.toString(), "-g", game.getTranslatedAbbreviation(), continueGame ? "-r" : "").start();
    }

    @Override
    public String getDisplayName() {
        return "Irony Mod Manager";
    }
}

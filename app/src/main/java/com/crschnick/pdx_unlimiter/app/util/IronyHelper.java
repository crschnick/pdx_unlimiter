package com.crschnick.pdx_unlimiter.app.util;

import com.crschnick.pdx_unlimiter.app.core.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.core.settings.Settings;
import com.crschnick.pdx_unlimiter.app.installation.Game;

import java.io.IOException;

public class IronyHelper {

    public static void launchEntry(Game game, boolean continueGame) {
        var exe = Settings.getInstance().ironyDir.getValue().resolve("IronyModManager.exe");
        try {
            new ProcessBuilder(exe.toString(), "-g", game.getAbbreviation(), continueGame ? "-r" : "").start();
        } catch (IOException e) {
            ErrorHandler.handleException(e);
        }
    }
}

package com.crschnick.pdx_unlimiter.app.game;

import com.crschnick.pdx_unlimiter.app.installation.Settings;

public class LaunchHelper {

    public static void launchGame(GameInstallation install) {
        if (Settings.getInstance().startSteam()) {

        }
    }
}

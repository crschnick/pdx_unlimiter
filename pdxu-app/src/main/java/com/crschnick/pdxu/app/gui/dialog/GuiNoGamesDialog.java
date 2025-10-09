package com.crschnick.pdxu.app.gui.dialog;

import com.crschnick.pdxu.app.core.window.AppDialog;
import com.crschnick.pdxu.app.installation.GameInstallation;
import com.crschnick.pdxu.app.prefs.AppPrefs;

public class GuiNoGamesDialog {

    public static void showIfNeeded() {
        if (!GameInstallation.ALL.isEmpty()) {
            return;
        }

        AppDialog.confirm("noValidInstallation");
        AppPrefs.get().selectCategory("games");
    }
}

package com.crschnick.pdx_unlimiter.app.savegame_mgr;

import javafx.application.Platform;
import org.apache.commons.lang3.function.FailableRunnable;

public class ErrorHandler {

    public static void handleException(Exception ex, boolean isTerminal) {
        handleException(ex, isTerminal, () -> {});
    }

    public static void handleException(Exception ex, boolean isTerminal, FailableRunnable<Exception> handler) {
        Exception t = null;
        try {
            handler.run();
        } catch (Exception tt) {
            t = tt;
        }
        Platform.runLater(() -> DialogHelper.showException(ex, true));
        if (t != null) {
            Exception finalT = t;
            Platform.runLater(() -> DialogHelper.showException(finalT, true));
        }
        if (isTerminal) {
            try {
                SavegameManagerApp.getAPP().close(false);
            } catch (Exception e) {
                Platform.runLater(() -> DialogHelper.showException(e, true));
                System.exit(1);
            }
        }
    }
}

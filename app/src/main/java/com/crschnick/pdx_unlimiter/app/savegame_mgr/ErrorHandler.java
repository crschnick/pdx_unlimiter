package com.crschnick.pdx_unlimiter.app.savegame_mgr;

import com.crschnick.pdx_unlimiter.app.DialogHelper;
import com.crschnick.pdx_unlimiter.app.SavegameManagerApp;
import com.crschnick.pdx_unlimiter.app.installation.PdxuInstallation;
import io.sentry.Sentry;
import javafx.application.Platform;
import org.apache.commons.lang3.function.FailableRunnable;

public class ErrorHandler {

    public static void handleStartupExcetion(Exception ex) {
        ex.printStackTrace();
        Sentry.capture(ex);
        System.exit(1);
    }

    public static void handleException(Exception ex, boolean isTerminal) {
        handleException(ex, isTerminal, () -> {
        });
    }

    public static void handleException(Exception ex, boolean isTerminal, FailableRunnable<Exception> handler) {
        Exception t = null;
        try {
            handler.run();
        } catch (Exception tt) {
            t = tt;
        }
        Platform.runLater(() -> {
            if (DialogHelper.showException(ex)) {
                ex.printStackTrace();
                Sentry.capture(ex);
            }
        });
        if (t != null) {
            Exception finalT = t;
            Platform.runLater(() -> {
                if (DialogHelper.showException(finalT)) {
                    finalT.printStackTrace();
                    Sentry.capture(finalT);
                }
            });
        }
        if (isTerminal) {
            try {
                SavegameManagerApp.getAPP().close(false);
            } catch (Exception e) {
                Platform.runLater(() -> {
                    if (DialogHelper.showException(e)) {
                        e.printStackTrace();
                        Sentry.capture(e);
                    }
                });
                System.exit(1);
            }
        }
    }
}

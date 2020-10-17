package com.crschnick.pdx_unlimiter.app.savegame_mgr;

import io.sentry.Sentry;
import javafx.application.Platform;
import org.apache.commons.lang3.function.FailableRunnable;

public class ErrorHandler {

    public static void init() {
        Sentry.init("https://cff56f4c1d624f46b64f51a8301d3543@o462618.ingest.sentry.io/5466262");
    }

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
        Platform.runLater(() -> {
            if(DialogHelper.showException(ex)) {
                Sentry.capture(ex);
            }
        });
        if (t != null) {
            Exception finalT = t;
            Platform.runLater(() -> {
                if(DialogHelper.showException(finalT)) {
                    Sentry.capture(finalT);
                }
            });
        }
        if (isTerminal) {
            try {
                SavegameManagerApp.getAPP().close(false);
            } catch (Exception e) {
                Platform.runLater(() -> {
                    if(DialogHelper.showException(e)) {
                        Sentry.capture(e);
                    }
                });
                System.exit(1);
            }
        }
    }
}

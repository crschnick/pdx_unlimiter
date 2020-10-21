package com.crschnick.pdx_unlimiter.app.savegame_mgr;

import com.crschnick.pdx_unlimiter.app.DialogHelper;
import com.crschnick.pdx_unlimiter.app.SavegameManagerApp;
import com.crschnick.pdx_unlimiter.app.installation.PdxuInstallation;
import io.sentry.Sentry;
import javafx.application.Platform;
import org.apache.commons.lang3.function.FailableRunnable;
import org.slf4j.LoggerFactory;

public class ErrorHandler {

    public static void handleStartupExcetion(Exception ex) {
        ex.printStackTrace();
        LoggerFactory.getLogger(ErrorHandler.class).error("Error", ex);
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
                LoggerFactory.getLogger(ErrorHandler.class).error("Error", ex);
                Sentry.capture(ex);
            }
        });
        if (t != null) {
            Exception finalT = t;
            Platform.runLater(() -> {
                if (DialogHelper.showException(finalT)) {
                    LoggerFactory.getLogger(ErrorHandler.class).error("Error", finalT);
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
                        LoggerFactory.getLogger(ErrorHandler.class).error("Error", e);
                        Sentry.capture(e);
                    }
                });
                System.exit(1);
            }
        }
    }
}

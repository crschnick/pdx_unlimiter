package com.crschnick.pdx_unlimiter.app.savegame_mgr;

import com.crschnick.pdx_unlimiter.app.DialogHelper;
import com.crschnick.pdx_unlimiter.app.SavegameManagerApp;
import com.crschnick.pdx_unlimiter.app.installation.PdxuInstallation;
import io.sentry.Sentry;
import javafx.application.Platform;
import org.apache.commons.lang3.function.FailableRunnable;
import org.slf4j.LoggerFactory;

public class ErrorHandler {

    private static boolean startupCompleted = false;

    public static void setStartupCompleted() {
        startupCompleted = true;
    }

    private static void handleExcetionWithoutPlatform(Exception ex) {
        ex.printStackTrace();
        LoggerFactory.getLogger(ErrorHandler.class).error("Error", ex);
        Sentry.capture(ex);
        System.exit(1);
    }

    public static void handleException(Exception ex) {
        if (!startupCompleted) {
            handleExcetionWithoutPlatform(ex);
        }

        Platform.runLater(() -> {
            if (DialogHelper.showException(ex)) {
                LoggerFactory.getLogger(ErrorHandler.class).error("Error", ex);
                Sentry.capture(ex);
            }
        });
    }

    public static void handleTerminalException(Exception ex) {
        if (!startupCompleted) {
            handleExcetionWithoutPlatform(ex);
        }

        Platform.runLater(() -> {
            if (DialogHelper.showException(ex)) {
                LoggerFactory.getLogger(ErrorHandler.class).error("Error", ex);
                Sentry.capture(ex);
            }

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
        });
    }
}

package com.crschnick.pdx_unlimiter.app.installation;

import com.crschnick.pdx_unlimiter.app.gui.DialogHelper;
import io.sentry.Sentry;
import javafx.application.Platform;
import org.slf4j.LoggerFactory;

public class ErrorHandler {

    private static boolean startupCompleted = false;

    public static void setStartupCompleted() {
        startupCompleted = true;
    }

    private static void handleExcetionWithoutPlatform(Exception ex) {
        ex.printStackTrace();
        Sentry.capture(ex);
    }

    public static void handleException(Exception ex) {
        if (!startupCompleted) {
            handleExcetionWithoutPlatform(ex);
        }

        Runnable run = () -> {
            LoggerFactory.getLogger(ErrorHandler.class).error("Error", ex);
            if (DialogHelper.showException(ex)) {
                Sentry.capture(ex);
            }
        };
        if (Platform.isFxApplicationThread()) {
            run.run();
        } else {
            Platform.runLater(run);
        }
    }

    public static void handleTerminalException(Exception ex) {
        if (!startupCompleted) {
            handleExcetionWithoutPlatform(ex);
        }

        Runnable run = () -> {
            LoggerFactory.getLogger(ErrorHandler.class).error("Error", ex);
            if (DialogHelper.showException(ex)) {
                Sentry.capture(ex);
            }
            System.exit(1);
        };
        if (Platform.isFxApplicationThread()) {
            run.run();
        } else {
            Platform.runLater(run);
        }
    }
}

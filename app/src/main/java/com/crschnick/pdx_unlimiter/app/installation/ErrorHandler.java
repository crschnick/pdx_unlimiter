package com.crschnick.pdx_unlimiter.app.installation;

import com.crschnick.pdx_unlimiter.app.gui.DialogHelper;
import io.sentry.Sentry;
import javafx.application.Platform;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.LoggerFactory;

public class ErrorHandler {

    private static boolean startupCompleted = false;

    public static void init() {
        startupCompleted = true;

        System.setProperty("sentry.dsn", "https://cff56f4c1d624f46b64f51a8301d3543@o462618.ingest.sentry.io/5466262");
        System.setProperty("sentry.stacktrace.hidecommon", "false");
        System.setProperty("sentry.stacktrace.app.packages", "");
        System.setProperty("sentry.uncaught.handler.enabled", "true");
        if (PdxuInstallation.getInstance().isProduction()) {
            System.setProperty("sentry.environment", "production");
            System.setProperty("sentry.release", PdxuInstallation.getInstance().getVersion()
                    + ", " + System.getProperty("os.name"));
        } else {
            System.setProperty("sentry.environment", "dev");
        }
        Sentry.init();
    }

    private static void handleExcetionWithoutPlatform(Exception ex) {
        ex.printStackTrace();
        LoggerFactory.getLogger(ErrorHandler.class).error("Error", ex);
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

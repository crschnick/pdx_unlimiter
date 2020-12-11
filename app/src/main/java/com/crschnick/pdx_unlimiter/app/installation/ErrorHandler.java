package com.crschnick.pdx_unlimiter.app.installation;

import com.crschnick.pdx_unlimiter.app.gui.DialogHelper;
import com.crschnick.pdx_unlimiter.app.gui.GuiErrorReporter;
import io.sentry.*;
import io.sentry.transport.StdoutTransport;
import javafx.application.Platform;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;
import java.util.Map;

public class ErrorHandler {

    private static boolean startupCompleted = false;

    public static void init() {
        startupCompleted = true;

        if (!PdxuInstallation.getInstance().isProduction()) {
            return;
        }

        LoggerFactory.getLogger(ErrorHandler.class).info("Initializing error handler");
        Sentry.init(sentryOptions -> {
            sentryOptions.setEnvironment("production");
            sentryOptions.setServerName(System.getProperty("os.name"));
            sentryOptions.setRelease(PdxuInstallation.getInstance().getVersion());
            sentryOptions.setDsn("https://cff56f4c1d624f46b64f51a8301d3543@sentry.io/5466262");
        });

        Sentry.configureScope(scope -> {
            LogManager.getInstance().getLogFile().ifPresent(l -> {
                scope.addAttachment(new Attachment(l.toString()));
            });
        });

        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            handleException(e);
        });

        LoggerFactory.getLogger(ErrorHandler.class).info("Finished initializing error handler\n");
    }

    private static void handleExcetionWithoutInit(Throwable ex) {
        ex.printStackTrace();
        LoggerFactory.getLogger(ErrorHandler.class).error("Init error", ex);
        if (PdxuInstallation.getInstance() == null || PdxuInstallation.getInstance().isProduction()) {
            Sentry.init(sentryOptions -> {
                sentryOptions.setDsn("https://cff56f4c1d624f46b64f51a8301d3543@sentry.io/5466262");
            });
        }
        Sentry.setExtra("initError", "true");
        Sentry.captureException(ex);
    }

    public static void handleException(Throwable ex) {
        handleException(ex, "An error occured", null);
    }

    public static void handleException(Throwable ex, String msg, Path attachFile) {
        if (!startupCompleted) {
            handleExcetionWithoutInit(ex);
        }

        Runnable run = () -> {
            LoggerFactory.getLogger(ErrorHandler.class).error(msg, ex);
            if (PdxuInstallation.getInstance().isProduction()) {
                if (GuiErrorReporter.showException(ex)) {
                    Sentry.withScope(scope -> {
                        if (attachFile != null) {
                            scope.addAttachment(new Attachment(attachFile.toString()));
                        }
                        Sentry.captureException(ex);
                    });
                }
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
            handleExcetionWithoutInit(ex);
        }

        LoggerFactory.getLogger(ErrorHandler.class).error("Terminal Error", ex);
        if (PdxuInstallation.getInstance().isProduction()) {
            if (GuiErrorReporter.showException(ex)) {
                Sentry.captureException(ex);
            }
        }
        System.exit(1);
    }
}

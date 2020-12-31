package com.crschnick.pdx_unlimiter.app.installation;

import com.crschnick.pdx_unlimiter.app.gui.GuiErrorReporter;
import io.sentry.Attachment;
import io.sentry.Sentry;
import javafx.application.Platform;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class ErrorHandler {

    private static boolean startupCompleted = false;

    public static void init() {
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

        registerThread(Thread.currentThread());

        LoggerFactory.getLogger(ErrorHandler.class).info("Finished initializing error handler\n");
    }

    public static void setPlatformInitialized() {
        startupCompleted = true;
    }

    public static void registerThread(Thread thread) {
        thread.setUncaughtExceptionHandler((t, e) -> {
            handleException(e, "An uncaught exception was thrown", null);
        });
    }

    private static void handleExcetionWithoutInit(Throwable ex) {
        ex.printStackTrace();
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
            return;
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
        if (PdxuInstallation.getInstance() == null || PdxuInstallation.getInstance().isProduction()) {
            if (GuiErrorReporter.showException(ex)) {
                Sentry.captureException(ex);
            }
        }
        System.exit(1);
    }
}

package com.crschnick.pdxu.app.core;

import com.crschnick.pdxu.app.gui.dialog.GuiErrorReporter;
import com.crschnick.pdxu.app.util.SupportedOs;
import com.crschnick.pdxu.app.util.ThreadHelper;
import io.sentry.Sentry;
import io.sentry.SentryEvent;
import javafx.application.Platform;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.CountDownLatch;

public class ErrorHandler {

    private static boolean errorReporterShowing = false;
    private static boolean platformInitialized = false;
    private static boolean platformShutdown = false;

    private static String replaceUserPaths(String msg) {
        switch (SupportedOs.get()) {
            case WINDOWS -> {
                return msg.replaceAll("\\\\Users\\\\.+?\\\\", "\\\\<UserDir>\\\\");
            }
            case LINUX -> {
                return msg.replaceAll("/home/.+?/", "/<UserDir>/");
            }
            case MAC -> {
                return msg.replaceAll("/Users/.+?/", "/<UserDir>/");
            }
            default -> {
                return msg;
            }
        }
    }

    private static void clearUserPaths(SentryEvent event) {
        if (event.getExceptions() == null) {
            return;
        }

        for (var ex : event.getExceptions()) {
            if (ex.getValue() != null) {
                ex.setValue(replaceUserPaths(ex.getValue()));
            }
        }
    }

    private static void setOptions() {
        Sentry.init(sentryOptions -> {
            sentryOptions.setEnvironment("production");
            sentryOptions.setServerName(System.getProperty("os.name"));
            sentryOptions.setRelease(PdxuInstallation.getInstance().getVersion());
            sentryOptions.setDsn("https://cff56f4c1d624f46b64f51a8301d3543@sentry.io/5466262");
            sentryOptions.setTag("standalone", String.valueOf(PdxuInstallation.getInstance().isStandalone()));
            sentryOptions.setBeforeSend((event, hint) -> {
                clearUserPaths(event);
                return event;
            });

            if (!PdxuInstallation.getInstance().isProduction()) {
                sentryOptions.setTracesSampleRate(null);
            } else if (PdxuInstallation.getInstance().isPreRelease()) {
                sentryOptions.setTracesSampleRate(1.0);
            }
        });
    }

    public static void init() {
        if (!PdxuInstallation.getInstance().isProduction()) {
            return;
        }

        LoggerFactory.getLogger(ErrorHandler.class).info("Initializing error handler");

        setOptions();
        registerThread(Thread.currentThread());

        var f = PdxuInstallation.getInstance().getSettingsLocation().resolve("error_exit");
        try {
            Files.createDirectories(f.getParent());
            Files.writeString(f, "false");
        } catch (IOException ex) {
            LoggerFactory.getLogger(ErrorHandler.class).error("Could not write error_exit file", ex);
        }

        var uf = PdxuInstallation.getInstance().getSettingsLocation().resolve("update");
        try {
            Files.createDirectories(uf.getParent());
            Files.writeString(uf, "false");
        } catch (IOException ex) {
            LoggerFactory.getLogger(ErrorHandler.class).error("Could not write update file", ex);
        }

        LoggerFactory.getLogger(ErrorHandler.class).info("Finished initializing error handler\n");
    }

    public static void setPlatformInitialized() {
        platformInitialized = true;
    }

    public static void setPlatformShutdown() {
        platformShutdown = true;
    }

    public static void registerThread(Thread thread) {
        thread.setUncaughtExceptionHandler((t, e) -> {
            handleException(e, "An uncaught exception was thrown");
        });
    }

    private static void unpreparedStartup(Throwable ex) {
        ex.printStackTrace();
        if (PdxuInstallation.getInstance() == null || PdxuInstallation.getInstance().isProduction()) {
            Sentry.init(sentryOptions -> {
                sentryOptions.setDsn("https://cff56f4c1d624f46b64f51a8301d3543@sentry.io/5466262");
            });
        }
        Sentry.setExtra("initError", "true");
        Sentry.captureException(ex);

        try {
            CountDownLatch latch = new CountDownLatch(1);
            Platform.startup(latch::countDown);
            try {
                latch.await();
            } catch (InterruptedException ignored) {
            }
        } catch (Throwable r) {
            // Check if platform initialization has failed
            r.printStackTrace();
            platformShutdown = true;
            return;
        }

        platformInitialized = true;
    }

    public static void handleTerminalException(Throwable ex) {
        handleException(ex, null, true);
    }

    public static void handleException(Throwable ex) {
        handleException(ex, "An error occured");
    }

    public static void handleException(Throwable ex, String msg) {
        handleException(ex, msg, false);
    }

    private static CountDownLatch showErrorReporter(Throwable ex, boolean terminal) {
        CountDownLatch latch = new CountDownLatch(1);
        Runnable run = () -> {
            boolean show = (PdxuInstallation.getInstance() == null ||
                    PdxuInstallation.getInstance().isProduction()) && !errorReporterShowing;
            if (show) {
                errorReporterShowing = true;
                GuiErrorReporter.showException(ex, terminal);
                errorReporterShowing = false;
            }

            latch.countDown();
        };
        if (Platform.isFxApplicationThread()) {
            run.run();
        } else {
            Platform.runLater(run);
        }
        return latch;
    }

    private static void handleException(Throwable ex, String msg, boolean terminal) {
        if (ex == null) {
            return;
        }

        if (!platformInitialized) {
            unpreparedStartup(ex);
        } else {
            if (LogManager.getInstance() != null) {
                LoggerFactory.getLogger(ErrorHandler.class).error(msg, ex);
            }
        }

        if (!platformShutdown) {
            var latch = showErrorReporter(ex, terminal);
            if (terminal) {
                try {
                    latch.await();
                } catch (InterruptedException ignored) {
                }
            }
        } else {
            if (LogManager.getInstance() != null) {
                LoggerFactory.getLogger(ErrorHandler.class).error(msg, ex);
            }
        }

        if (terminal) {
            // Wait to send error report
            ThreadHelper.sleep(1000);
            System.exit(1);
        }
    }
}

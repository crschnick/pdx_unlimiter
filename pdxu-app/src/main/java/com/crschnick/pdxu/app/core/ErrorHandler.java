package com.crschnick.pdxu.app.core;

import com.crschnick.pdxu.app.gui.dialog.GuiErrorReporter;
import com.crschnick.pdxu.app.util.ThreadHelper;
import io.sentry.Attachment;
import io.sentry.Sentry;
import io.sentry.UserFeedback;
import javafx.application.Platform;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;

public class ErrorHandler {

    private static boolean errorReporterShowing = false;
    private static boolean startupCompleted = false;

    private static void setOptions() {
        Sentry.init(sentryOptions -> {
            sentryOptions.setEnvironment("production");
            sentryOptions.setServerName(System.getProperty("os.name"));
            sentryOptions.setRelease(PdxuInstallation.getInstance().getVersion());
            sentryOptions.setDsn("https://cff56f4c1d624f46b64f51a8301d3543@sentry.io/5466262");

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

    private static void unpreparedStartup(Throwable ex) {
        ex.printStackTrace();
        if (PdxuInstallation.getInstance() == null || PdxuInstallation.getInstance().isProduction()) {
            Sentry.init(sentryOptions -> {
                sentryOptions.setDsn("https://cff56f4c1d624f46b64f51a8301d3543@sentry.io/5466262");
            });
        }
        Sentry.setExtra("initError", "true");
        Sentry.captureException(ex);

        CountDownLatch latch = new CountDownLatch(1);
        Platform.startup(latch::countDown);
        try {
            latch.await();
        } catch (InterruptedException ignored) {
        }
        startupCompleted = true;
    }

    public static void handleTerminalException(Exception ex) {
        handleException(ex, null, null, true);
    }

    public static void handleException(Throwable ex) {
        handleException(ex, "An error occured", null);
    }

    public static void handleException(Throwable ex, String msg, Path attachFile) {
        handleException(ex, msg, attachFile, false);
    }

    private static void handleException(Throwable ex, String msg, Path attachFile, boolean terminal) {
        if (!startupCompleted) {
            unpreparedStartup(ex);
        } else {
            LoggerFactory.getLogger(ErrorHandler.class).error(msg, ex);
        }

        CountDownLatch latch = new CountDownLatch(1);
        Runnable run = () -> {
            boolean show = (PdxuInstallation.getInstance() == null ||
                    PdxuInstallation.getInstance().isProduction()) && !errorReporterShowing;
            if (show) {
                errorReporterShowing = true;
                boolean shouldSendDiagnostics = GuiErrorReporter.showException(ex, terminal);
                reportError(ex, shouldSendDiagnostics, attachFile);
                errorReporterShowing = false;
            }

            latch.countDown();
        };
        if (Platform.isFxApplicationThread()) {
            run.run();
        } else {
            Platform.runLater(run);
        }

        if (terminal) {
            try {
                latch.await();
            } catch (InterruptedException ignored) {
            }

            if (PdxuInstallation.getInstance() != null &&
                    PdxuInstallation.getInstance().getSettingsLocation() != null) {
                var f = PdxuInstallation.getInstance().getSettingsLocation().resolve("error_exit");
                try {
                    Files.createDirectories(f.getParent());
                    Files.writeString(f, "true");
                } catch (IOException ignored) {
                }
            }

            // Wait to send error report
            ThreadHelper.sleep(1000);

            System.exit(1);
        }
    }

    public static void reportError(Throwable t, boolean diag, Path attachFile) {
        if (diag) {
            Sentry.withScope(scope -> {
                LogManager.getInstance().getLogFile().ifPresent(l -> {
                    scope.addAttachment(new Attachment(l.toString()));
                });
                if (attachFile != null) {
                    scope.addAttachment(new Attachment(attachFile.toString()));
                }
                scope.setTag("diagnoticsData", "true");
                Sentry.captureException(t);
            });
        } else {
            Sentry.withScope(scope -> {
                scope.setTag("diagnoticsData", "false");
                Sentry.captureException(t);
            });
        }
    }

    public static void reportIssue(Path attachFile) {
        Runnable run = () -> {
            var r = GuiErrorReporter.showIssueDialog();
            r.ifPresent(msg -> {
                Sentry.withScope(scope -> {
                    LogManager.getInstance().getLogFile().ifPresent(l -> {
                        scope.addAttachment(new Attachment(l.toString()));
                    });
                    if (attachFile != null) {
                        scope.addAttachment(new Attachment(attachFile.toString()));
                    }

                    var id = Sentry.captureMessage("User Issue Report");
                    Sentry.captureUserFeedback(new UserFeedback(id, null, null, msg));
                });
            });
        };
        if (Platform.isFxApplicationThread()) {
            run.run();
        } else {
            Platform.runLater(run);
        }
    }
}

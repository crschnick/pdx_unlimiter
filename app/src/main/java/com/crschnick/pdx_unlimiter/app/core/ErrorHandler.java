package com.crschnick.pdx_unlimiter.app.core;

import com.crschnick.pdx_unlimiter.app.gui.dialog.GuiErrorReporter;
import com.crschnick.pdx_unlimiter.app.util.ThreadHelper;
import io.sentry.Attachment;
import io.sentry.Sentry;
import io.sentry.UserFeedback;
import javafx.application.Platform;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;
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
        });
    }

    public static void init() {
        if (!PdxuInstallation.getInstance().isProduction()) {
            return;
        }

        LoggerFactory.getLogger(ErrorHandler.class).info("Initializing error handler");
        setOptions();

        registerThread(Thread.currentThread());

        LoggerFactory.getLogger(ErrorHandler.class).info("Finished initializing error handler\n");
    }

    private static void rakalyTokenReport(List<String> tokens, Path file) {
        Sentry.init(sentryOptions -> {
            sentryOptions.setServerName(System.getProperty("os.name"));
            sentryOptions.setDsn("https://6b540a8824bc4d3d887061488598700e@o510976.ingest.sentry.io/5607403");
        });

        Sentry.withScope(scope -> {
            scope.addAttachment(new Attachment(file.toString()));
            Sentry.captureMessage("Unknown tokens: " + String.join(", ", tokens));
        });

        setOptions();
    }

    public static void reportRakalyTokens(List<String> tokens, Path file) {
        Runnable run = () -> {
            var r = GuiErrorReporter.showRakalyTokenDialog();
            if (r) {
                rakalyTokenReport(tokens, file);
            }
        };
        if (Platform.isFxApplicationThread()) {
            run.run();
        } else {
            Platform.runLater(run);
        }
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
        } catch (InterruptedException e) {
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
        }

        CountDownLatch latch = new CountDownLatch(1);
        Runnable run = () -> {
            LoggerFactory.getLogger(ErrorHandler.class).error(msg, ex);
            if (PdxuInstallation.getInstance() == null ||
                    PdxuInstallation.getInstance().isProduction() && !errorReporterShowing) {
                errorReporterShowing = true;
                if (GuiErrorReporter.showException(ex, terminal)) {
                    Sentry.withScope(scope -> {
                        LogManager.getInstance().getLogFile().ifPresent(l -> {
                            scope.addAttachment(new Attachment(l.toString()));
                        });
                        if (attachFile != null) {
                            scope.addAttachment(new Attachment(attachFile.toString()));
                        }
                        Sentry.captureException(ex);
                    });
                }
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
            } catch (InterruptedException e) {
            }
            // Wait to send error report
            ThreadHelper.sleep(1000);
            System.exit(1);
        }
    }

    public static void reportIssue() {
        Runnable run = () -> {
            var r = GuiErrorReporter.showIssueDialog();
            r.ifPresent(msg -> {
                Sentry.withScope(scope -> {
                    LogManager.getInstance().getLogFile().ifPresent(l -> {
                        scope.addAttachment(new Attachment(l.toString()));
                    });

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

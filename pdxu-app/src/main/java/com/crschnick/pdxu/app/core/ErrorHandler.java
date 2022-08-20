package com.crschnick.pdxu.app.core;

import com.crschnick.pdxu.app.gui.dialog.GuiErrorReporter;
import com.crschnick.pdxu.app.util.SupportedOs;
import com.crschnick.pdxu.app.util.ThreadHelper;
import io.sentry.Attachment;
import io.sentry.Sentry;
import io.sentry.SentryEvent;
import io.sentry.UserFeedback;
import io.sentry.protocol.SentryId;
import javafx.application.Platform;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
        platformInitialized = true;
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

    private static CountDownLatch showErrorReporter(Throwable ex, Path attachFile, boolean terminal) {
        CountDownLatch latch = new CountDownLatch(1);
        Runnable run = () -> {
            boolean show = (PdxuInstallation.getInstance() == null ||
                    PdxuInstallation.getInstance().isProduction()) && !errorReporterShowing;
            if (show) {
                errorReporterShowing = true;
                boolean shouldSendDiagnostics = GuiErrorReporter.showException(ex, terminal);
                reportError(ex, shouldSendDiagnostics, attachFile, terminal);
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

    private static void handleException(Throwable ex, String msg, Path attachFile, boolean terminal) {
        if (!platformInitialized) {
            unpreparedStartup(ex);
        } else {
            LoggerFactory.getLogger(ErrorHandler.class).error(msg, ex);
        }

        if (!platformShutdown) {
            var latch = showErrorReporter(ex, attachFile, terminal);
            if (terminal) {
                try {
                    latch.await();
                } catch (InterruptedException ignored) {
                }
            }
        }

        if (terminal) {
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

    public static void reportError(Throwable t, boolean diag, Path attachFile, boolean terminal) {
        if (diag) {
            AtomicReference<SentryId> id = new AtomicReference<>();
            Sentry.withScope(scope -> {
                LogManager.getInstance().getLogFile().ifPresent(l -> {
                    scope.addAttachment(new Attachment(l.toString()));
                });
                scope.setTag("diagnoticsData", "true");
                Sentry.setExtra("terminal", String.valueOf(terminal));
                id.set(Sentry.captureException(t));
            });
            if (attachFile != null) {
                addAttachment(id.get(), attachFile);
            }
        } else {
            Sentry.withScope(scope -> {
                scope.setTag("diagnoticsData", "false");
                Sentry.setExtra("terminal", String.valueOf(terminal));
                Sentry.captureException(t);
            });
        }
    }

    private static void addAttachment(SentryId id, Path attachFile) {
        if (!Files.exists(attachFile)) {
            return;
        }

        try {
            var bytes = Files.readAllBytes(attachFile);
            var out = new ByteArrayOutputStream();
            var zipName = "pdxu-report-" + new Random().nextInt(Integer.MAX_VALUE) + ".zip";
            try (var zipOut = new ZipOutputStream(out)) {
                zipOut.putNextEntry(new ZipEntry(attachFile.getFileName().toString()));
                zipOut.write(bytes);
            }

            for (var part : splitInPartsIfNeeded(zipName, out.toByteArray())) {
                Sentry.withScope(scope -> {
                    scope.setTag("id", id.toString());
                    scope.addAttachment(new Attachment(part.toString()));
                    Sentry.captureMessage("Attachment");
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<Path> splitInPartsIfNeeded(String prefix, byte[] bytes) throws IOException {
        var length = 9_000_000;
        if (bytes.length <= length) {
            Files.write(FileUtils.getTempDirectory().toPath().resolve(prefix), bytes);
            return List.of(FileUtils.getTempDirectory().toPath().resolve(prefix));
        }

        List<Path> files = new ArrayList<>();
        for (int i = 0; i < Math.ceil((double) bytes.length / length); i++) {
            var file = FileUtils.getTempDirectory().toPath().resolve(prefix + ".part" + i);
            try (var out = Files.newOutputStream(file)) {
                out.write(bytes, i * length, Math.min(length, bytes.length - (i * length)));
            }
            files.add(file);
        }
        return files;
    }

    public static void reportIssue(Path attachFile) {
        Runnable run = () -> {
            var r = GuiErrorReporter.showIssueDialog();
            r.ifPresent(msg -> {
                AtomicReference<SentryId> id = new AtomicReference<>();
                Sentry.withScope(scope -> {
                    LogManager.getInstance().getLogFile().ifPresent(l -> {
                        scope.addAttachment(new Attachment(l.toString()));
                    });

                    id.set(Sentry.captureMessage("User Issue Report"));
                    Sentry.captureUserFeedback(new UserFeedback(id.get(), null, null, msg));
                });
                if (attachFile != null) {
                    addAttachment(id.get(), attachFile);
                }
            });
        };
        if (Platform.isFxApplicationThread()) {
            run.run();
        } else {
            Platform.runLater(run);
        }
    }
}

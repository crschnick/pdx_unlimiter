package com.crschnick.pdxu.app.issue;

import com.crschnick.pdxu.app.core.AppLogs;
import com.crschnick.pdxu.app.core.AppProperties;
import com.crschnick.pdxu.app.core.mode.AppOperationMode;
import com.crschnick.pdxu.app.update.AppDistributionType;

import io.sentry.*;
import io.sentry.protocol.Geo;
import io.sentry.protocol.SentryId;
import io.sentry.protocol.User;
import org.apache.commons.io.FileUtils;

import java.nio.file.Files;
import java.util.stream.Collectors;

public class SentryErrorHandler implements ErrorHandler {

    private static final ErrorHandler INSTANCE = new SyncErrorHandler(new SentryErrorHandler());
    private boolean init;

    public static ErrorHandler getInstance() {
        return INSTANCE;
    }

    private static boolean hasUserReport(ErrorEvent ee) {
        var email = ee.getEmail();
        var hasEmail = email != null && !email.isBlank();
        var text = ee.getUserReport();
        var hasText = text != null && !text.isBlank();
        return hasEmail || hasText;
    }

    private static boolean doesExceedCommentSize(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        return text.length() > 5000;
    }

    private static SentryId captureEvent(ErrorEvent ee) {
        if (!hasUserReport(ee) && "User Report".equals(ee.getDescription())) {
            return null;
        }

        if (ee.getThrowable() != null) {
            return Sentry.captureException(ee.getThrowable(), sc -> fillScope(ee, sc));
        }

        if (ee.getDescription() != null) {
            return Sentry.captureMessage(ee.getDescription(), sc -> fillScope(ee, sc));
        }

        var event = new SentryEvent();
        return Sentry.captureEvent(event, sc -> fillScope(ee, sc));
    }

    private static void fillScope(ErrorEvent ee, IScope s) {
        if (ee.isShouldSendDiagnostics()) {
            // Write all buffered output to log files to ensure that we get all information
            if (AppLogs.get() != null) {
                AppLogs.get().flush();
            }

            var atts = ee.getAttachments().stream()
                    .map(d -> {
                        try {
                            var toUse = d;
                            if (Files.isDirectory(d)) {
                                toUse = AttachmentHelper.compressZipfile(
                                        d,
                                        FileUtils.getTempDirectory()
                                                .toPath()
                                                .resolve(d.getFileName().toString() + ".zip"));
                            }
                            return new Attachment(toUse.toString());
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            return null;
                        }
                    })
                    .filter(attachment -> attachment != null)
                    .toList();
            atts.forEach(attachment -> s.addAttachment(attachment));
        }

        if (doesExceedCommentSize(ee.getUserReport())) {
            try {
                var report = Files.createTempFile("report", ".txt");
                Files.writeString(report, ee.getUserReport());
                s.addAttachment(new Attachment(report.toString()));
            } catch (Exception ex) {
                AppLogs.get().logException("Unable to create report file", ex);
            }
        }

        s.setTag("terminal", Boolean.toString(ee.isTerminal()));
        s.setTag("omitted", Boolean.toString(ee.isOmitted()));
        s.setTag(
                "logs",
                Boolean.toString(
                        ee.isShouldSendDiagnostics() && !ee.getAttachments().isEmpty()));
        s.setTag("inStartup", Boolean.toString(AppOperationMode.isInStartup()));
        s.setTag("inShutdown", Boolean.toString(AppOperationMode.isInShutdown()));
        s.setTag("unhandled", Boolean.toString(ee.isUnhandled()));
        s.setTag("diagnostics", Boolean.toString(ee.isShouldSendDiagnostics()));
        s.setTag("initial", AppProperties.get() != null ? AppProperties.get().isInitialLaunch() + "" : "false");

        var exMessage = ee.getThrowable() != null ? ee.getThrowable().getMessage() : null;
        if (ee.getDescription() != null && !ee.getDescription().equals(exMessage) && ee.isShouldSendDiagnostics()) {
            s.setTag("message", ee.getDescription().lines().collect(Collectors.joining(" ")));
        }

        var user = new User();
        user.setId(AppProperties.get().getUuid().toString());
        user.setGeo(new Geo());
        s.setUser(user);
    }

    public void handle(ErrorEvent ee) {
        // Assume that this object is wrapped by a synchronous error handler
        if (!init) {
            AppProperties.init();
            if (AppProperties.get().getSentryUrl() != null) {
                Sentry.init(options -> {
                    options.setDsn(AppProperties.get().getSentryUrl());
                    options.setEnableUncaughtExceptionHandler(false);
                    options.setAttachServerName(false);
                    options.setRelease(AppProperties.get().getVersion());
                    options.setEnableShutdownHook(false);
                    options.setTag("os", System.getProperty("os.name"));
                    options.setTag("osVersion", System.getProperty("os.version"));
                    options.setTag("arch", AppProperties.get().getArch());
                    options.setDist(AppDistributionType.get().getId());
                    options.setSendModules(false);
                    options.setAttachThreads(false);
                    options.setEnableDeduplication(false);
                    options.setCacheDirPath(
                            AppProperties.get().getDataDir().resolve("cache").toString());
                });
            }
            init = true;
        }

        var id = captureEvent(ee);
        if (id == null) {
            return;
        }

        var email = ee.getEmail();
        var hasEmail = email != null && !email.isBlank();
        var text = ee.getUserReport();
        if (hasUserReport(ee)) {
            var fb = new UserFeedback(id);
            if (hasEmail) {
                fb.setEmail(email);
            }
            if (doesExceedCommentSize(text)) {
                fb.setComments("<Attachment>");
            } else {
                fb.setComments(text);
            }
            Sentry.captureUserFeedback(fb);
        }
        Sentry.flush(3000);
    }
}

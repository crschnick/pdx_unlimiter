package com.crschnick.pdx_unlimiter.app.savegame_mgr;

import com.crschnick.pdx_unlimiter.app.DialogHelper;
import com.crschnick.pdx_unlimiter.app.SavegameManagerApp;
import io.sentry.Sentry;
import javafx.application.Platform;
import org.apache.commons.lang3.function.FailableRunnable;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class ErrorHandler {

    public static void init() {
        try {
            FieldUtils.writeStaticField(LoggerFactory.class, "INITIALIZATION_STATE", 4, true);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        try {
            InputStream in = Files.newInputStream(Path.of("sentry.properties"));
            System.getProperties().load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Path p = Path.of(System.getProperty("user.dir")).getParent().resolve("version");
            if (Files.exists(p)) {
                String v = Files.readString(p);
                System.setProperty("sentry.release", v);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Sentry.init();
    }

    public static void handleException(Exception ex, boolean isTerminal) {
        handleException(ex, isTerminal, () -> {
        });
    }

    public static void handleException(Exception ex, boolean isTerminal, FailableRunnable<Exception> handler) {
        Exception t = null;
        try {
            handler.run();
        } catch (Exception tt) {
            t = tt;
        }
        Platform.runLater(() -> {
            if (DialogHelper.showException(ex)) {
                Sentry.capture(ex);
            }
        });
        if (t != null) {
            Exception finalT = t;
            Platform.runLater(() -> {
                if (DialogHelper.showException(finalT)) {
                    Sentry.capture(finalT);
                }
            });
        }
        if (isTerminal) {
            try {
                SavegameManagerApp.getAPP().close(false);
            } catch (Exception e) {
                Platform.runLater(() -> {
                    if (DialogHelper.showException(e)) {
                        Sentry.capture(e);
                    }
                });
                System.exit(1);
            }
        }
    }
}

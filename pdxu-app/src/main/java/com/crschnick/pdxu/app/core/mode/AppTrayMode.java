package com.crschnick.pdxu.app.core.mode;

import com.crschnick.pdxu.app.core.AppTray;
import com.crschnick.pdxu.app.issue.*;
import com.crschnick.pdxu.app.platform.PlatformInit;
import com.crschnick.pdxu.app.platform.PlatformThread;

import java.awt.*;

public class AppTrayMode extends AppOperationMode {

    @Override
    public boolean isSupported() {
        return SystemTray.isSupported();
    }

    @Override
    public void onSwitchTo() throws Throwable {
        AppOperationMode.BACKGROUND.onSwitchTo();
        PlatformInit.init(true);

        PlatformThread.runLaterIfNeededBlocking(() -> {
            if (AppTray.get() == null) {
                TrackEvent.info("Initializing tray");
                AppTray.init();
            }

            AppTray.get().show();
            TrackEvent.info("Finished tray initialization");
        });
    }

    @Override
    public String getId() {
        return "tray";
    }

    @Override
    public void onSwitchFrom() {
        if (AppTray.get() != null) {
            TrackEvent.info("Closing tray");
            PlatformThread.runLaterIfNeededBlocking(() -> AppTray.get().hide());
        }
    }

    @Override
    public ErrorHandler getErrorHandler() {
        var log = new LogErrorHandler();
        return new SyncErrorHandler(event -> {
            // Check if tray initialization is finished
            if (AppTray.get() != null) {
                AppTray.get().getErrorHandler().handle(event);
            }
            log.handle(event);
            ErrorAction.ignore().handle(event);
        });
    }

    @Override
    public void finalTeardown() throws Throwable {
        onSwitchFrom();
        BACKGROUND.finalTeardown();
    }
}

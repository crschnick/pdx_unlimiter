package com.crschnick.pdxu.app.core;

import com.crschnick.pdxu.app.core.mode.AppOperationMode;
import com.crschnick.pdxu.app.issue.ErrorEventFactory;
import com.crschnick.pdxu.app.platform.PlatformState;
import com.crschnick.pdxu.app.util.OsType;

import java.awt.*;
import java.awt.desktop.*;
import java.util.List;

public class AppDesktopIntegration {

    public static void init() {
        try {
            // This will initialize the toolkit on macOS and create the dock icon
            // macOS does not like applications that run fully in the background, so always do it
            if (OsType.ofLocal() == OsType.MACOS && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().setPreferencesHandler(e -> {
                    if (PlatformState.getCurrent() != PlatformState.RUNNING) {
                        return;
                    }

                    if (AppLayoutModel.get() != null) {
                        AppLayoutModel.get().selectSettings();
                    }
                });

                // URL open operations have to be handled in a special way on macOS!
                Desktop.getDesktop().setOpenURIHandler(e -> {
                    AppOpenArguments.handle(List.of(e.getURI().toString()));
                });

                Desktop.getDesktop().addAppEventListener(new AppReopenedListener() {
                    @Override
                    public void appReopened(AppReopenedEvent e) {
                        AppOperationMode.switchToAsync(AppOperationMode.GUI);
                    }
                });
            }
        } catch (Throwable ex) {
            ErrorEventFactory.fromThrowable(ex).term().handle();
        }
    }
}

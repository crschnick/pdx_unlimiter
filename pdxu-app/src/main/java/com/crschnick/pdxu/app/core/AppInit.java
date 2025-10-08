package com.crschnick.pdxu.app.core;

import com.crschnick.pdxu.app.core.beacon.AppBeacon;
import com.crschnick.pdxu.app.core.check.AppDebugModeCheck;
import com.crschnick.pdxu.app.core.mode.AppOperationMode;
import com.crschnick.pdxu.app.core.window.AppMainWindow;
import com.crschnick.pdxu.app.issue.ErrorEventFactory;
import com.crschnick.pdxu.app.issue.TrackEvent;
import com.crschnick.pdxu.app.platform.PlatformInit;
import com.crschnick.pdxu.app.platform.PlatformThreadWatcher;
import com.crschnick.pdxu.app.prefs.AppPrefs;
import com.crschnick.pdxu.app.update.AppDistributionType;
import com.crschnick.pdxu.app.util.GlobalTimer;
import com.crschnick.pdxu.app.util.ModuleLayerLoader;
import com.crschnick.pdxu.app.util.ThreadHelper;

import javafx.application.Platform;

import lombok.SneakyThrows;

public class AppInit {

    private static void handleUncaught(Thread thread, Throwable ex) {
        // It seems like a few exceptions are thrown in the quantum renderer
        // when in shutdown. We can ignore these
        if (AppOperationMode.isInShutdown() && Platform.isFxApplicationThread() && ex instanceof NullPointerException) {
            return;
        }

        // There are some accessibility exceptions on macOS, nothing we can do about that
        if (Platform.isFxApplicationThread()
                && ex instanceof NullPointerException
                && ex.getMessage() != null
                && ex.getMessage().contains("Accessible")) {
            ErrorEventFactory.fromThrowable(ex)
                    .expected()
                    .description(
                            "An error occurred with the Accessibility implementation. A screen reader might not be supported right now")
                    .build()
                    .handle();
            return;
        }

        // Handle any startup uncaught errors
        if (AppOperationMode.isInStartup() && thread.threadId() == 1) {
            ex.printStackTrace();
            AppOperationMode.halt(1);
        }

        if (ex instanceof OutOfMemoryError) {
            ex.printStackTrace();
            AppOperationMode.halt(1);
        }

        ErrorEventFactory.fromThrowable(ex).unhandled(true).build().handle();
    }

    private static void setup(String[] args) {
        try {
            // Only for handling SIGTERM
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                AppOperationMode.externalShutdown();
            }));

            // Handle uncaught exceptions
            Thread.setDefaultUncaughtExceptionHandler((thread, ex) -> {
                handleUncaught(thread, ex);
            });

            TrackEvent.info("Initial setup");
            AppMainWindow.loadingText("initializingApp");
            GlobalTimer.init();
            AppProperties.init(args);
            PlatformThreadWatcher.init();
            AppLogs.init();
            AppDebugModeCheck.printIfNeeded();
            AppProperties.get().logArguments();
            AppDistributionType.init();
            ModuleLayerLoader.loadAll(ModuleLayer.boot(), t -> {
                ErrorEventFactory.fromThrowable(t).handle();
            });
            AppI18n.init();
            AppPrefs.initLocal();
            AppDataLock.init();
            AppBeacon.init();
            AppInstance.init();
            // Initialize early to load in parallel
            PlatformInit.init(false);
            ThreadHelper.runAsync(() -> {
                PlatformInit.init(true);
                AppMainWindow.init(getStartupMode() == AppOperationMode.GUI);
            });
            TrackEvent.info("Finished initial setup");
        } catch (Throwable ex) {
            ErrorEventFactory.fromThrowable(ex).term().handle();
        }
    }

    private static AppOperationMode getStartupMode() {
        var event = TrackEvent.withInfo("Startup mode determined");
        if (AppMainWindow.get() != null && AppMainWindow.get().getStage().isShowing()) {
            event.tag("mode", "gui").tag("reason", "windowShowing").handle();
            return AppOperationMode.GUI;
        }

        var prop = AppProperties.get().getExplicitMode();
        if (prop != null) {
            event.tag("mode", prop.getDisplayName())
                    .tag("reason", "modePropertyPassed")
                    .handle();
            return AppOperationMode.map(prop);
        }

        if (AppPrefs.get() != null) {
            var pref = AppPrefs.get().startupBehaviour().getValue().getMode();
            event.tag("mode", pref.getDisplayName())
                    .tag("reason", "prefSetting")
                    .handle();
            return AppOperationMode.map(pref);
        }

        event.tag("mode", "gui").tag("reason", "fallback").handle();
        return AppOperationMode.GUI;
    }

    @SneakyThrows
    public static void init(String[] args) {
        AppOperationMode.setInStartup(true);
        setup(args);

        if (AppProperties.get().isAotTrainMode()) {
            AppOperationMode.switchToSyncOrThrow(AppOperationMode.BACKGROUND);
            AppOperationMode.setInStartup(false);
            AppAotTrain.runTrainingMode();
            AppOperationMode.shutdown(false);
            return;
        }

        var startupMode = getStartupMode();
        AppOperationMode.switchToSyncOrThrow(startupMode);
        // If it doesn't find time, the JVM will not gc the startup workload
        System.gc();
        AppOperationMode.setInStartup(false);
        AppOpenArguments.init();
    }
}

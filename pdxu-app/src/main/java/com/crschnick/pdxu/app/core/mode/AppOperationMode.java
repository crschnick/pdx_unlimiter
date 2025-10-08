package com.crschnick.pdxu.app.core.mode;

import com.crschnick.pdxu.app.core.*;
import com.crschnick.pdxu.app.core.window.AppMainWindow;
import com.crschnick.pdxu.app.issue.*;
import com.crschnick.pdxu.app.platform.PlatformState;
import com.crschnick.pdxu.app.prefs.AppPrefs;
import com.crschnick.pdxu.app.prefs.CloseBehaviour;
import com.crschnick.pdxu.app.util.*;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.time.Duration;
import java.util.List;

public abstract class AppOperationMode {

    public static final AppOperationMode BACKGROUND = new AppBaseMode();
    public static final AppOperationMode TRAY = new AppTrayMode();
    public static final AppOperationMode GUI = new AppGuiMode();
    private static final List<AppOperationMode> ALL = List.of(BACKGROUND, TRAY, GUI);
    private static final Object HALT_LOCK = new Object();

    @Getter
    @Setter
    private static boolean inStartup;

    @Getter
    private static boolean inShutdown;

    @Getter
    private static boolean inShutdownHook;

    private static AppOperationMode CURRENT = null;

    public static AppOperationMode map(AppOperationModeSelection mode) {
        return switch (mode) {
            case BACKGROUND -> BACKGROUND;
            case TRAY -> TRAY;
            case GUI -> GUI;
        };
    }

    public static void externalShutdown() {
        // If we used System.exit(), we don't want to do this
        if (AppOperationMode.isInShutdown()) {
            return;
        }

        inShutdownHook = true;
        TrackEvent.info("Received SIGTERM externally");
        AppOperationMode.shutdown(false);
    }

    public static void switchToAsync(AppOperationMode newMode) {
        ThreadHelper.createPlatformThread("mode switcher", false, () -> {
                    switchToSyncIfPossible(newMode);
                })
                .start();
    }

    public static void switchToSyncOrThrow(AppOperationMode newMode) throws Throwable {
        TrackEvent.info("Attempting to switch mode to " + newMode.getId());

        if (!newMode.isSupported()) {
            throw PlatformState.getLastError() != null
                    ? PlatformState.getLastError()
                    : new IllegalStateException("Unsupported operation mode: " + newMode.getId());
        }

        set(newMode);
    }

    public static boolean switchToSyncIfPossible(AppOperationMode newMode) {
        TrackEvent.info("Attempting to switch mode to " + newMode.getId());

        if (newMode.equals(TRAY) && !TRAY.isSupported()) {
            TrackEvent.info("Tray is not available, using base instead");
            set(BACKGROUND);
            return false;
        }

        if (newMode.equals(GUI) && !GUI.isSupported()) {
            TrackEvent.info("Gui is not available, using base instead");
            set(BACKGROUND);
            return false;
        }

        set(newMode);
        return true;
    }

    public static void close() {
        set(null);
    }

    public static List<AppOperationMode> getAll() {
        return ALL;
    }

    public static void executeAfterShutdown(FailableRunnable<Exception> r) {
        Runnable exec = () -> {
            if (inShutdown) {
                return;
            }

            try {
                if (!isInStartup()) {
                    inShutdown = true;
                    if (CURRENT != null) {
                        CURRENT.finalTeardown();
                    }
                    CURRENT = null;
                }

                r.run();
            } catch (Throwable ex) {
                ErrorEventFactory.fromThrowable(ex).handle();
                AppOperationMode.halt(1);
            }

            // In case we perform any operations such as opening a terminal
            // give it some time to open while this process is still alive
            // Otherwise it might quit because the parent process is dead already
            ThreadHelper.sleep(100);
            AppOperationMode.halt(0);
        };

        // Creates separate non daemon thread to force execution after shutdown even if current thread is a daemon
        var t = new Thread(exec);
        t.setDaemon(false);
        t.start();
    }

    public static void halt(int code) {
        synchronized (HALT_LOCK) {
            TrackEvent.info("Halting now!");
            AppLogs.teardown();
            Runtime.getRuntime().halt(code);
        }
    }

    public static void onWindowClose() {
        CloseBehaviour action;
        if (AppPrefs.get() != null && !isInStartup() && !isInShutdown()) {
            action = AppPrefs.get().closeBehaviour().getValue();
        } else {
            action = CloseBehaviour.QUIT;
        }
        ThreadHelper.runAsync(() -> {
            action.run();
        });
    }

    @SneakyThrows
    public static void shutdown(boolean hasError) {
        if (isInStartup()) {
            TrackEvent.info("Received shutdown request while in startup. Halting ...");
            AppOperationMode.halt(1);
        }

        TrackEvent.info("Starting shutdown ...");

        synchronized (AppOperationMode.class) {
            if (inShutdown) {
                return;
            }

            inShutdown = true;
        }

        // Keep a non-daemon thread running
        var thread = ThreadHelper.createPlatformThread("shutdown", false, () -> {
            try {
                if (CURRENT != null) {
                    CURRENT.finalTeardown();
                }
                CURRENT = null;
            } catch (Throwable t) {
                ErrorEventFactory.fromThrowable(t).term().handle();
                AppOperationMode.halt(1);
            }

            AppOperationMode.halt(hasError ? 1 : 0);
        });
        thread.start();

        // Use a timer to always exit after some time in case we get stuck
        var limit = !hasError && !AppProperties.get().isDevelopmentEnvironment() ? 25000 : Integer.MAX_VALUE;
        var exited = thread.join(Duration.ofMillis(limit));
        if (!exited) {
            TrackEvent.info("Shutdown took too long. Halting ...");
            AppOperationMode.halt(1);
        }
    }

    private static synchronized void set(AppOperationMode newMode) {
        if (inShutdown) {
            return;
        }

        if (CURRENT == null && newMode == null) {
            return;
        }

        if (CURRENT != null && CURRENT.equals(newMode)) {
            return;
        }

        try {
            if (newMode == null) {
                shutdown(false);
                return;
            }

            if (CURRENT != null && CURRENT != BACKGROUND) {
                CURRENT.onSwitchFrom();
            }

            BACKGROUND.onSwitchTo();
            if (CURRENT != GUI
                    && newMode != GUI
                    && AppMainWindow.get() != null
                    && AppMainWindow.get().getStage().isShowing()) {
                GUI.onSwitchTo();
                newMode = GUI;
            } else {
                newMode.onSwitchTo();
            }
            CURRENT = newMode;
        } catch (Throwable ex) {
            ErrorEventFactory.fromThrowable(ex).terminal(true).build().handle();
        }
    }

    public static AppOperationMode get() {
        return CURRENT;
    }

    public abstract boolean isSupported();

    public abstract String getId();

    public abstract void onSwitchTo() throws Throwable;

    public abstract void onSwitchFrom();

    public abstract void finalTeardown() throws Throwable;

    public ErrorHandler getErrorHandler() {
        return new SyncErrorHandler(new GuiErrorHandler());
    }
}

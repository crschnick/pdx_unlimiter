package com.crschnick.pdxu.app.core;

import com.crschnick.pdxu.app.PdxuApp;
import com.crschnick.pdxu.app.core.settings.SavedState;
import com.crschnick.pdxu.app.core.settings.Settings;
import com.crschnick.pdxu.app.gui.dialog.GuiErrorReporter;
import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.app.installation.GameAppManager;
import com.crschnick.pdxu.app.installation.GameInstallation;
import com.crschnick.pdxu.app.lang.LanguageManager;
import com.crschnick.pdxu.app.lang.PdxuI18n;
import com.crschnick.pdxu.app.savegame.FileImporter;
import com.crschnick.pdxu.app.savegame.SavegameStorage;
import com.crschnick.pdxu.app.savegame.SavegameWatcher;
import com.crschnick.pdxu.app.util.integration.PdxToolsWebHelper;
import javafx.application.Platform;
import org.apache.commons.lang3.SystemUtils;
import org.jnativehook.GlobalScreen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ComponentManager {

    private static Logger logger;

    public static void initialSetup(List<String> inputs) {
        try {
            PdxuInstallation.checkDataDirectoryPermissions();
            PdxuInstallation.init();

            LogManager.init();
            logger = LoggerFactory.getLogger(ComponentManager.class);

            PdxuI18n.initDefault();
            ErrorHandler.init();
            IntegrityManager.init();

            inputs.forEach(FileImporter::addToImportQueue);
            PdxuInstallation.checkCorrectExtraction();
            if (!PdxuInstallation.shouldStart()) {
                System.exit(0);
            }

            // Start task executor early such that a shutdown can be performed from now on!
            TaskExecutor.getInstance().start();
        } catch (Throwable e) {
            ErrorHandler.handleTerminalException(e);
        }
    }

    public static void initialPlatformSetup() {
        Platform.setImplicitExit(false);
        Platform.runLater(() -> ErrorHandler.registerThread(Thread.currentThread()));

        try {
            // Load saved state before window creation so that stored window coordinates can be used
            SavedState.init();
            PdxuApp.getApp().setupWindowState();
            // Load languages after window setup since it can create error windows to notify the user
            LanguageManager.init();
            // Load settings after window setup since settings entries can create dialog windows to notify the user
            Settings.init();
            PdxuApp.getApp().setupBasicWindowContent();
        } catch (Throwable e) {
            ErrorHandler.handleTerminalException(e);
        }

        TaskExecutor.getInstance().submitTask(ComponentManager::init, false);
        TaskExecutor.getInstance().submitTask(ComponentManager::initialFinalSetup, false);
    }

    private static void initialFinalSetup() {
        PdxuApp.getApp().setupCompleteWindowContent();
    }

    public static void switchGame(Game game) {
        SavedState.getInstance().setActiveGame(game);
        SavegameManagerState.get().selectGameAsync(game);
    }

    public static void reloadSettings(Runnable settingsUpdater) {
        TaskExecutor.getInstance().stopAndWait();
        TaskExecutor.getInstance().start();
        TaskExecutor.getInstance().submitTask(() -> {
            reset();
            Settings.getInstance().update(settingsUpdater);
            init();
        }, true);
    }

    public static void finalTeardown() {
        TaskExecutor.getInstance().stop(() -> {
            ErrorHandler.setPlatformShutdown();
            ComponentManager.reset();
            Platform.exit();
        });
    }

    private static void registerNativeHook() {
        try {
            if (PdxuInstallation.getInstance().isNativeHookEnabled() && !SystemUtils.IS_OS_MAC) {
                GlobalScreen.registerNativeHook();
            }
        } catch (Throwable ex) {
            GuiErrorReporter.showSimpleErrorMessage("Unable to register native hook.\n" +
                    "This might be a permissions issue with your system. " +
                    "In-game keyboard shortcuts will be unavailable!");
            logger.warn("Unable to register native hook", ex);
        }
    }

    private static void unregisterNativeHook() {
        try {
            if (PdxuInstallation.getInstance().isNativeHookEnabled() && !SystemUtils.IS_OS_MAC) {
                GlobalScreen.unregisterNativeHook();
            }
        } catch (Throwable ex) {
            GuiErrorReporter.showSimpleErrorMessage("Unable to unregister native hook.\n" +
                    "This might be a permissions issue with your system.");
            logger.warn("Unable to unregister native hook", ex);
        }
    }

    private static void init() {
        logger.debug("Initializing ...");
        try {
            CacheManager.init();
            PdxuI18n.init();
            GameInstallation.init();
            SavegameStorage.init();
            SavegameManagerState.init();

            FileWatchManager.init();
            SavegameWatcher.init();
            GameAppManager.init();

            FileImporter.init();

            EditorProvider.get().init();
            registerNativeHook();
        } catch (Throwable e) {
            ErrorHandler.handleTerminalException(e);
        }
        logger.debug("Finished initialization");
    }

    private static void reset() {
        logger.debug("Resetting program state ...");
        try {
            SavedState.getInstance().saveConfig();

            FileWatchManager.reset();
            SavegameManagerState.reset();

            logger.debug("Waiting for platform thread");
            // Sync with platform thread
            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(latch::countDown);
            latch.await();
            logger.debug("Synced with platform thread");

            GameAppManager.reset();
            SavegameWatcher.reset();
            SavegameStorage.reset();
            GameInstallation.reset();
            unregisterNativeHook();
            PdxuI18n.reset();
            CacheManager.reset();
            PdxToolsWebHelper.reset();
        } catch (Throwable e) {
            ErrorHandler.handleTerminalException(e);
        }
        logger.debug("Reset completed");
    }
}

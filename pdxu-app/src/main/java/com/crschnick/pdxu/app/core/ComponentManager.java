package com.crschnick.pdxu.app.core;

import com.crschnick.pdxu.app.PdxuApp;
import com.crschnick.pdxu.app.core.settings.SavedState;
import com.crschnick.pdxu.app.core.settings.Settings;
import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.app.installation.GameAppManager;
import com.crschnick.pdxu.app.installation.GameInstallation;
import com.crschnick.pdxu.app.lang.LanguageManager;
import com.crschnick.pdxu.app.lang.PdxuI18n;
import com.crschnick.pdxu.app.savegame.FileImporter;
import com.crschnick.pdxu.app.savegame.SavegameStorage;
import com.crschnick.pdxu.app.savegame.SavegameWatcher;
import com.crschnick.pdxu.app.util.integration.RakalyWebHelper;
import javafx.application.Platform;
import org.jnativehook.GlobalScreen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

public class ComponentManager {

    private static Logger logger;

    public static void initialSetup(String[] args) {
        try {
            PdxuInstallation.init();

            LogManager.init();
            logger = LoggerFactory.getLogger(ComponentManager.class);

            PdxuI18n.initDefault();
            ErrorHandler.init();
            IntegrityManager.init();

            logger.info("Running pdxu with arguments: " + Arrays.toString(args));
            Arrays.stream(args).forEach(FileImporter::addToImportQueue);
            if (!PdxuInstallation.shouldStart()) {
                System.exit(0);
            }
        } catch (Exception e) {
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
        } catch (Exception e) {
            ErrorHandler.handleTerminalException(e);
        }

        TaskExecutor.getInstance().start();
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
            ComponentManager.reset();
            Platform.exit();
        });
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
            if (PdxuInstallation.getInstance().isNativeHookEnabled()) {
                GlobalScreen.registerNativeHook();
            }
        } catch (Exception e) {
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
            // Sync with platform thread after GameIntegration reset
            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(latch::countDown);
            latch.await();
            logger.debug("Synced with platform thread");

            GameAppManager.reset();
            SavegameWatcher.reset();
            SavegameStorage.reset();
            GameInstallation.reset();
            if (PdxuInstallation.getInstance().isNativeHookEnabled()) {
                GlobalScreen.unregisterNativeHook();
            }
            PdxuI18n.reset();
            CacheManager.reset();
            RakalyWebHelper.shutdownServer();
        } catch (Exception e) {
            ErrorHandler.handleTerminalException(e);
        }
        logger.debug("Reset completed");
    }
}

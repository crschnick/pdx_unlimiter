package com.crschnick.pdx_unlimiter.app.core;

import com.crschnick.pdx_unlimiter.app.PdxuApp;
import com.crschnick.pdx_unlimiter.app.core.settings.SavedState;
import com.crschnick.pdx_unlimiter.app.core.settings.Settings;
import com.crschnick.pdx_unlimiter.app.editor.EditorExternalState;
import com.crschnick.pdx_unlimiter.app.gui.game.GameImage;
import com.crschnick.pdx_unlimiter.app.installation.Game;
import com.crschnick.pdx_unlimiter.app.installation.GameAppManager;
import com.crschnick.pdx_unlimiter.app.installation.GameInstallation;
import com.crschnick.pdx_unlimiter.app.savegame.FileImporter;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameCollection;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameStorage;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameWatcher;
import com.crschnick.pdx_unlimiter.core.info.SavegameInfo;
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
            // Load settings after window setup since settings entries can create dialog windows to notify the user
            Settings.init();
            PdxuApp.getApp().setupBasicWindowContent();
        } catch (Exception e) {
            ErrorHandler.handleTerminalException(e);
        }

        TaskExecutor.getInstance().start();
        TaskExecutor.getInstance().submitTask(ComponentManager::init, false);
    }

    public static void switchGame(Game game) {
        CacheManager.getInstance().onSelectedGameChange();
        SavegameManagerState.get().selectGame(game);
        SavedState.getInstance().setActiveGame(game);
    }

    public static <T, I extends SavegameInfo<T>> void selectCollection(SavegameCollection<T, I> col) {
        CacheManager.getInstance().onSelectedSavegameCollectionChange();
        SavegameManagerState.<T, I>get().selectCollection(col);
    }

    public static void reloadSettings(Runnable settingsUpdater) {
        TaskExecutor.getInstance().stopAndWait();
        TaskExecutor.getInstance().start();
        TaskExecutor.getInstance().submitTask(() -> {
            reset();
            settingsUpdater.run();
            Settings.check();
            Settings.save();
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
            GameInstallation.init();
            SavegameStorage.init();
            SavegameManagerState.init();

            GameImage.init();
            SavegameWatcher.init();

            GameAppManager.init();
            FileImporter.init();

            CacheManager.init();

            FileWatchManager.init();
            EditorExternalState.init();
            if (PdxuInstallation.getInstance().isNativeHookEnabled()) {
                GlobalScreen.registerNativeHook();
            }

            PdxuApp.getApp().setupCompleteWindowContent();
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
            CacheManager.reset();

            logger.debug("Waiting for platform thread");
            // Sync with platform thread after GameIntegration reset
            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(latch::countDown);
            latch.await();
            logger.debug("Synced with platform thread");

            SavegameWatcher.reset();
            SavegameStorage.reset();
            GameInstallation.reset();
            if (PdxuInstallation.getInstance().isNativeHookEnabled()) {
                GlobalScreen.unregisterNativeHook();
            }
        } catch (Exception e) {
            ErrorHandler.handleException(e);
        }
        logger.debug("Reset completed");
    }
}

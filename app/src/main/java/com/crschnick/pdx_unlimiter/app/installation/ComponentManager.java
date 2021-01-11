package com.crschnick.pdx_unlimiter.app.installation;

import com.crschnick.pdx_unlimiter.app.PdxuApp;
import com.crschnick.pdx_unlimiter.app.game.GameAppManager;
import com.crschnick.pdx_unlimiter.app.game.GameInstallation;
import com.crschnick.pdx_unlimiter.app.game.GameIntegration;
import com.crschnick.pdx_unlimiter.app.game.SavegameManagerState;
import com.crschnick.pdx_unlimiter.app.gui.GameImage;
import com.crschnick.pdx_unlimiter.app.gui.GuiLayout;
import com.crschnick.pdx_unlimiter.app.savegame.FileImporter;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameCache;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameWatcher;
import javafx.application.Platform;
import org.jnativehook.GlobalScreen;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

public class ComponentManager {

    public static void initialSetup(String[] args) {
        try {
            PdxuInstallation.init();
            LogManager.init();
            ErrorHandler.init();

            LoggerFactory.getLogger(PdxuApp.class).info("Running pdxu with arguments: " + Arrays.toString(args));
            Arrays.stream(args).forEach(FileImporter::addToImportQueue);

            if (!PdxuInstallation.shouldStart()) {
                System.exit(0);
            }
        } catch (Exception e) {
            ErrorHandler.handleTerminalException(e);
        }
    }

    public static void additionalSetup() {
        ErrorHandler.setPlatformInitialized();
        Platform.setImplicitExit(false);
        Platform.runLater(() -> ErrorHandler.registerThread(Thread.currentThread()));

        TaskExecutor.getInstance().start();
        TaskExecutor.getInstance().submitTask(ComponentManager::init, true);
    }

    public static void reloadSettings(Settings newS) {
        TaskExecutor.getInstance().stopAndWait();
        TaskExecutor.getInstance().start();
        TaskExecutor.getInstance().submitTask(() -> {
            reset();
            Settings.updateSettings(newS);
            init();
        }, true);
    }

    public static void finalTeardown() {
        TaskExecutor.getInstance().stop(() -> {
            ComponentManager.reset();
            Platform.exit();

            // Explicitly exit, because in case of an OOM error some Thread might always be blocking the exit
            // Somehow causes weird javafx errors, so its disabled
            // System.exit(0);
        });
    }

    private static void init() {
        LoggerFactory.getLogger(ComponentManager.class).debug("Initializing ...");
        try {
            Settings.init();

            GuiLayout.init();

            GameInstallation.init();

            SavedState.init();
            PdxuApp.getApp().setupWindowState();

            GameImage.init();
            SavegameCache.init();
            SavegameWatcher.init();

            GameAppManager.init();
            FileImporter.init();

            GameIntegration.init();
            SavegameManagerState.init();

            FileWatchManager.init();
            if (PdxuInstallation.getInstance().isNativeHookEnabled()) {
                GlobalScreen.registerNativeHook();
            }
        } catch (Exception e) {
            ErrorHandler.handleTerminalException(e);
        }
        LoggerFactory.getLogger(ComponentManager.class).debug("Finished initialization");
    }

    private static void reset() {
        LoggerFactory.getLogger(ComponentManager.class).debug("Resetting program state ...");
        try {
            SavedState.getInstance().saveConfig();

            FileWatchManager.reset();
            SavegameManagerState.reset();
            GameIntegration.reset();

            LoggerFactory.getLogger(ComponentManager.class).debug("Waiting for platform thread");
            // Sync with platform thread after GameIntegration reset
            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(latch::countDown);
            latch.await();
            LoggerFactory.getLogger(ComponentManager.class).debug("Synced with platform thread");

            SavegameWatcher.reset();
            SavegameCache.reset();
            GameInstallation.reset();
            if (PdxuInstallation.getInstance().isNativeHookEnabled()) {
                GlobalScreen.unregisterNativeHook();
            }
        } catch (Exception e) {
            ErrorHandler.handleException(e);
        }
        LoggerFactory.getLogger(ComponentManager.class).debug("Reset completed");
    }
}

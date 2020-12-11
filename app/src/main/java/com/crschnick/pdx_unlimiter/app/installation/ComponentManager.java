package com.crschnick.pdx_unlimiter.app.installation;

import com.crschnick.pdx_unlimiter.app.PdxuApp;
import com.crschnick.pdx_unlimiter.app.achievement.AchievementManager;
import com.crschnick.pdx_unlimiter.app.game.GameAppManager;
import com.crschnick.pdx_unlimiter.app.game.GameInstallation;
import com.crschnick.pdx_unlimiter.app.game.GameIntegration;
import com.crschnick.pdx_unlimiter.app.gui.GameImage;
import com.crschnick.pdx_unlimiter.app.gui.GuiLayout;
import com.crschnick.pdx_unlimiter.app.savegame.FileImporter;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameCache;
import org.jnativehook.GlobalScreen;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Arrays;

public class ComponentManager {

    public static void initialSetup(String[] args) {
        try {
            PdxuInstallation.init();
            LogManager.init();
            ErrorHandler.init();

            LoggerFactory.getLogger(PdxuApp.class).info("Running pdxu with arguments: " + Arrays.toString(args));
            Arrays.stream(args)
                    .map(Path::of)
                    .forEach(FileImporter::addToImportQueue);

            if (!PdxuInstallation.shouldStart()) {
                System.exit(0);
            }
        } catch (Exception e) {
            ErrorHandler.handleTerminalException(e);
        }
    }

    public static void additionalSetup() {
        TaskExecutor.start();
        TaskExecutor.getInstance().submitTask(ComponentManager::init);
    }

    public static void reloadSettings() {
        TaskExecutor.stopAndWait();
        reset();
        Settings.getInstance().apply();
        init();
        TaskExecutor.start();
    }

    public static void finalTeardown() {
        TaskExecutor.getInstance().submitTask(ComponentManager::reset);
        TaskExecutor.stopAndWait();
    }

    private static void init() {
        try {
            Settings.init();

            GuiLayout.init();

            GameInstallation.init();
            GameImage.init();
            AchievementManager.init();
            SavegameCache.init();

            GameAppManager.init();
            FileImporter.init();

            GameIntegration.init();

            if (PdxuInstallation.getInstance().isNativeHookEnabled()) {
                GlobalScreen.registerNativeHook();
            }
        } catch (Exception e) {
            ErrorHandler.handleTerminalException(e);
        }
    }

    private static void reset() {
        try {
            GameIntegration.reset();
            SavegameCache.reset();
            GameInstallation.reset();
            if (PdxuInstallation.getInstance().isNativeHookEnabled()) {
                GlobalScreen.unregisterNativeHook();
            }
        } catch (Exception e) {
            ErrorHandler.handleException(e);
        }

    }
}

package com.crschnick.pdxu.app.core.mode;

import com.crschnick.pdxu.app.core.*;
import com.crschnick.pdxu.app.core.beacon.AppBeacon;
import com.crschnick.pdxu.app.core.check.*;
import com.crschnick.pdxu.app.core.window.AppDialog;
import com.crschnick.pdxu.app.core.window.AppMainWindow;
import com.crschnick.pdxu.app.core.window.AppWindowTitle;
import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.installation.GameAppManager;
import com.crschnick.pdxu.app.installation.GameCacheManager;
import com.crschnick.pdxu.app.installation.GameInstallation;
import com.crschnick.pdxu.app.issue.TrackEvent;
import com.crschnick.pdxu.app.platform.PlatformInit;
import com.crschnick.pdxu.app.platform.PlatformState;
import com.crschnick.pdxu.app.prefs.AppPrefs;
import com.crschnick.pdxu.app.savegame.SavegameStorage;
import com.crschnick.pdxu.app.savegame.SavegameWatcher;
import com.crschnick.pdxu.app.update.UpdateAvailableDialog;
import com.crschnick.pdxu.app.util.EditorProvider;
import com.crschnick.pdxu.app.util.GlobalTimer;
import com.crschnick.pdxu.app.util.WindowsRegistry;

public class AppBaseMode extends AppOperationMode {

    private boolean initialized;

    @Override
    public boolean isSupported() {
        return true;
    }

    @Override
    public String getId() {
        return "background";
    }

    @Override
    public void onSwitchTo() {
        if (initialized) {
            return;
        }

        // For debugging error handling
        // if (true) throw new IllegalStateException();

        TrackEvent.info("Initializing base mode components ...");
        AppMainWindow.loadingText("initializingApp");
        AppWindowTitle.init();
        AppPathCorruptCheck.check();
        AppWindowsTempCheck.check();
        AppDirectoryPermissionsCheck.checkDirectory(AppSystemInfo.ofCurrent().getTemp());
        WindowsRegistry.init();
        AppJavaOptionsCheck.check();

        // If we downloaded an update, and decided to no longer automatically update, don't remind us!
        // You can still update manually in the about tab
        if (AppPrefs.get().automaticallyUpdate().get()) {
            UpdateAvailableDialog.showIfNeeded(true);
        }

        AppRosettaCheck.check();
        AppWindowsArmCheck.check();
        AppMainWindow.loadingText("loadingUserInterface");
        PlatformInit.init(true);
        AppImages.init();

        IntegrityManager.init();
        TaskExecutor.getInstance().start();
        SavegameStorage.init();
        AppPrefs.get().initInstallations();
        AppLayoutModel.init();
        AppLayoutModel.get().getActiveGame().ifPresent(game -> {
            GameImage.loadGameImages(game);
        });

        GameCacheManager.init();
        AppFileWatcher.init();
        SavegameWatcher.init();
        GameAppManager.init();
        EditorProvider.get().init();
        AppNativeHook.registerNativeHook();

        AppMainWindow.initContent();

        TrackEvent.info("Waiting for startup dialogs to close");
        AppDialog.waitForAllDialogsClose();

        AppConfigurationDialog.showIfNeeded();

        TrackEvent.info("Finished base components initialization");
        initialized = true;
    }

    @Override
    public void onSwitchFrom() {}

    @Override
    public void finalTeardown() {
        AppFileWatcher.reset();
        GameAppManager.reset();
        SavegameWatcher.reset();
        SavegameStorage.reset();
        GameInstallation.reset();
        AppNativeHook.unregisterNativeHook();

        TrackEvent.withInfo("Base mode shutdown started").build();
        AppPrefs.reset();
        AppBeacon.reset();
        AppLayoutModel.reset();
        AppTheme.reset();
        PlatformState.reset();
        AppResources.reset();
        AppDataLock.reset();
        GlobalTimer.reset();
        TrackEvent.info("Base mode shutdown finished");
    }
}

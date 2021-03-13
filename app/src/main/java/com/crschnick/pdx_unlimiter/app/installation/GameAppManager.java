package com.crschnick.pdx_unlimiter.app.installation;

import com.crschnick.pdx_unlimiter.app.core.SavegameManagerState;
import com.crschnick.pdx_unlimiter.app.core.TaskExecutor;
import com.crschnick.pdx_unlimiter.app.core.settings.Settings;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameActions;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.awt.*;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class GameAppManager {

    private static final GameAppManager INSTANCE = new GameAppManager();

    private Instant lastImport;
    private final ObjectProperty<GameApp> activeGame = new SimpleObjectProperty<>(null);

    private GameAppManager() {
    }

    public static void init() {
        TaskExecutor.getInstance().submitLoop(() -> {
            INSTANCE.update();
        });
    }

    private void update() {
        if ((activeGame.get() == null)) {
            var process = GameInstallation.ALL.values().stream()
                    .map(g -> new GameApp(ProcessHandle.allProcesses()
                            .filter(p -> p.info().command()
                                    .map(cs -> isInstanceOfGame(cs, g)).orElse(false))
                            .findAny().orElse(null), g)
                    )
                    .filter(ga -> ga.getProcess() != null)
                    .findAny();
            if (process.isPresent()) {
                activeGame.set(process.get());
                activeGame.get().onStart();
                SavegameManagerState.get().selectGame(
                        GameInstallation.ALL.inverseBidiMap().get(process.get().getInstallation()));
            }
        } else {
            updateImportTimer();

            if (!activeGame.get().isAlive()) {
                activeGame.get().onShutdown();
                activeGame.set(null);
            }
        }
    }

    private void updateImportTimer() {
        if (!Settings.getInstance().enabledTimedImports.getValue()) {
            return;
        }

        if (lastImport == null) {
            lastImport = Instant.now();
        }

        if (Duration.between(lastImport, Instant.now()).compareTo(
                Duration.of(Settings.getInstance().timedImportsInterval.getValue(), ChronoUnit.MINUTES)) > 0) {
            if (Settings.getInstance().playSoundOnBackgroundImport.getValue()) {
                Toolkit.getDefaultToolkit().beep();
            }
            SavegameActions.importLatestSavegame();
            lastImport = Instant.now();
        }
    }

    private static boolean isInstanceOfGame(String cmd, GameInstallation install) {
        return cmd.contains(install.getExecutable().toString());
    }

    public static GameAppManager getInstance() {
        return INSTANCE;
    }

    public GameApp getActiveGame() {
        return activeGame.get();
    }

    public ObjectProperty<GameApp> activeGameProperty() {
        return activeGame;
    }
}

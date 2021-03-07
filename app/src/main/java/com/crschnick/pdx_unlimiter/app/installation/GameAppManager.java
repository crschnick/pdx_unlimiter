package com.crschnick.pdx_unlimiter.app.installation;

import com.crschnick.pdx_unlimiter.app.core.SavegameManagerState;
import com.crschnick.pdx_unlimiter.app.core.TaskExecutor;
import javafx.beans.property.SimpleObjectProperty;

public class GameAppManager {

    private static final GameAppManager INSTANCE = new GameAppManager();

    private final SimpleObjectProperty<GameApp> activeGame = new SimpleObjectProperty<>(null);

    private GameAppManager() {
    }

    public static void init() {
        TaskExecutor.getInstance().submitLoop(() -> {
            if ((INSTANCE.activeGame.get() == null)) {
                var process = GameInstallation.ALL.values().stream()
                        .map(g -> new GameApp(ProcessHandle.allProcesses()
                                .filter(p -> p.info().command()
                                        .map(cs -> isInstanceOfGame(cs, g)).orElse(false))
                                .findAny().orElse(null), g)
                        )
                        .filter(ga -> ga.getProcess() != null)
                        .findAny();
                if (process.isPresent()) {
                    INSTANCE.activeGame.set(process.get());
                    INSTANCE.activeGame.get().onStart();
                    SavegameManagerState.get().selectGame(
                            GameInstallation.ALL.inverseBidiMap().get(process.get().getInstallation()));
                }
            } else {
                if (!INSTANCE.activeGame.get().isAlive()) {
                    INSTANCE.activeGame.get().onShutdown();
                    INSTANCE.activeGame.set(null);
                }
            }
        });
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

    public SimpleObjectProperty<GameApp> activeGameProperty() {
        return activeGame;
    }
}

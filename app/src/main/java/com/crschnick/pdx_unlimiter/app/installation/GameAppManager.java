package com.crschnick.pdx_unlimiter.app.installation;

import com.crschnick.pdx_unlimiter.app.core.TaskExecutor;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameManagerState;
import javafx.beans.property.SimpleObjectProperty;

import java.util.Objects;
import java.util.stream.Stream;

public class GameAppManager {

    private static GameAppManager INSTANCE = new GameAppManager();

    private SimpleObjectProperty<GameApp> activeGame = new SimpleObjectProperty<>(null);

    private GameAppManager() {
    }

    public static void init() {
        TaskExecutor.getInstance().submitLoop(() -> {
            if ((INSTANCE.activeGame.get() == null)) {
                var process = Stream.of(GameInstallation.EU4, GameInstallation.HOI4,
                        GameInstallation.CK3, GameInstallation.STELLARIS)
                        .filter(Objects::nonNull)
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
                    SavegameManagerState.get().selectIntegration(
                            GameIntegration.getForInstallation(process.get().getInstallation()));
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
        if (cmd.contains(install.getExecutable().toString())) {
            return true;
        }

        return false;
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

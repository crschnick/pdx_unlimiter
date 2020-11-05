package com.crschnick.pdx_unlimiter.app.installation;

import com.crschnick.pdx_unlimiter.app.game.Eu4App;
import com.crschnick.pdx_unlimiter.app.game.GameInstallation;
import javafx.beans.property.SimpleObjectProperty;

import java.util.Optional;

public class GameManager {

    private static GameManager INSTANCE;

    private SimpleObjectProperty<Optional<PdxApp>> ACTIVE_GAME = new SimpleObjectProperty<>(Optional.empty());

    public static void init() {
        INSTANCE = new GameManager();
        Thread t = new Thread(() -> {
            while (true) {
                if (!INSTANCE.ACTIVE_GAME.get().isPresent()) {
                    Optional<ProcessHandle> h = ProcessHandle
                            .allProcesses()
                            .filter(p -> p.info().command()
                                    .map(c -> c.contains(GameInstallation.EU4.getExecutable().toString()))
                                    .orElse(false))
                            .findFirst();
                    if (h.isPresent()) {
                        INSTANCE.ACTIVE_GAME.set(Optional.of(new Eu4App(h.get())));
                        INSTANCE.ACTIVE_GAME.get().get().onStart();
                    }
                } else {
                    if (!INSTANCE.ACTIVE_GAME.get().get().isAlive()) {
                        INSTANCE.ACTIVE_GAME.get().get().onShutdown();
                        INSTANCE.ACTIVE_GAME.set(Optional.empty());
                    }
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        t.setDaemon(true);
        t.start();
    }

    public static GameManager getInstance() {
        return INSTANCE;
    }

    private GameManager() {
    }

    public Optional<PdxApp> getActiveGame() {
        return ACTIVE_GAME.get();
    }

    public SimpleObjectProperty<Optional<PdxApp>> activeGameProperty() {
        return ACTIVE_GAME;
    }
}

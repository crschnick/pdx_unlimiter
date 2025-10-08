package com.crschnick.pdxu.app.installation;

import com.crschnick.pdxu.app.core.AppProperties;
import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

public class GameApp {

    private final Game game;
    private final ProcessHandle process;
    private NativeKeyListener listener;

    public GameApp(ProcessHandle process, Game game) {
        this.process = process;
        this.game = game;
    }

    public void onStart() {
        if (AppProperties.get().isNativeHookEnabled()) {
            listener = new GameKeyListener(this, 5000);
            GlobalScreen.addNativeKeyListener(listener);
        }
    }

    public void onShutdown() {
        if (AppProperties.get().isNativeHookEnabled()) {
            GlobalScreen.removeNativeKeyListener(listener);
            listener = null;
        }
    }

    public Game getGame() {
        return game;
    }

    public ProcessHandle getProcess() {
        return process;
    }

    public boolean isAlive() {
        return process.isAlive();
    }

    public void kill() {
        process.destroyForcibly();
    }
}

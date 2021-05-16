package com.crschnick.pdxu.app.installation;

import com.crschnick.pdxu.app.core.PdxuInstallation;
import org.jnativehook.GlobalScreen;
import org.jnativehook.keyboard.NativeKeyListener;

public class GameApp {

    private final Game game;
    private final ProcessHandle process;
    private NativeKeyListener listener;

    public GameApp(ProcessHandle process, Game game) {
        this.process = process;
        this.game = game;
    }

    public void onStart() {
        if (PdxuInstallation.getInstance().isNativeHookEnabled()) {
            listener = new GameKeyListener(this);
            GlobalScreen.addNativeKeyListener(listener);
        }
    }

    public void onShutdown() {
        if (PdxuInstallation.getInstance().isNativeHookEnabled()) {
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

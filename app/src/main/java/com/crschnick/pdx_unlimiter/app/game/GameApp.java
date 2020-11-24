package com.crschnick.pdx_unlimiter.app.game;

import org.jnativehook.GlobalScreen;
import org.jnativehook.keyboard.NativeKeyListener;

public class GameApp {

    private NativeKeyListener listener;
    private GameInstallation installation;
    private ProcessHandle process;

    public GameApp(ProcessHandle process, GameInstallation installation) {
        this.process = process;
        this.installation = installation;
    }

    public void onStart() {
        listener = new GameKeyListener(this);
        GlobalScreen.addNativeKeyListener(listener);
    }

    public void onShutdown() {
        GlobalScreen.removeNativeKeyListener(listener);
        listener = null;
    }

    public GameInstallation getInstallation() {
        return installation;
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

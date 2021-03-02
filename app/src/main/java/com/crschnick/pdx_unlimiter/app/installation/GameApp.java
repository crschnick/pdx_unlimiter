package com.crschnick.pdx_unlimiter.app.installation;

import com.crschnick.pdx_unlimiter.app.core.PdxuInstallation;
import org.jnativehook.GlobalScreen;
import org.jnativehook.keyboard.NativeKeyListener;

public class GameApp {

    private NativeKeyListener listener;
    private final GameInstallation installation;
    private final ProcessHandle process;

    public GameApp(ProcessHandle process, GameInstallation installation) {
        this.process = process;
        this.installation = installation;
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

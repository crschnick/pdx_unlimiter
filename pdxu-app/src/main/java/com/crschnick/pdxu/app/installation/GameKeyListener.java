package com.crschnick.pdxu.app.installation;

import com.crschnick.pdxu.app.core.TaskExecutor;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.slf4j.LoggerFactory;

public class GameKeyListener implements NativeKeyListener {

    private final GameApp handle;

    public GameKeyListener(GameApp handle) {
        this.handle = handle;
    }

    public void nativeKeyPressed(NativeKeyEvent e) {
        if ((e.getModifiers() & NativeKeyEvent.SHIFT_MASK) != 0 && (e.getModifiers() & NativeKeyEvent.CTRL_MASK) != 0) {
            if (e.getKeyCode() == NativeKeyEvent.VC_K) {
                LoggerFactory.getLogger(GameKeyListener.class).debug("Kill key pressed");
                handle.kill();
            }
            if (e.getKeyCode() == NativeKeyEvent.VC_I) {
                LoggerFactory.getLogger(GameKeyListener.class).debug("Import key pressed");
                GameAppManager.getInstance().playImportSound();
                GameAppManager.getInstance().importLatest();
            }
            if (e.getKeyCode() == NativeKeyEvent.VC_C) {
                LoggerFactory.getLogger(GameKeyListener.class).debug("Checkpoint key pressed");
                GameAppManager.getInstance().loadLatestCheckpoint();
            }
            if (e.getKeyCode() == NativeKeyEvent.VC_R) {
                TaskExecutor.getInstance().submitTask(() -> {
                    LoggerFactory.getLogger(GameKeyListener.class).debug("Reverting to latest save");
                    GameAppManager.getInstance().importLatestAndLaunch();
                }, true);
            }
        }
    }

    public void nativeKeyReleased(NativeKeyEvent e) {
    }

    public void nativeKeyTyped(NativeKeyEvent e) {
    }
}

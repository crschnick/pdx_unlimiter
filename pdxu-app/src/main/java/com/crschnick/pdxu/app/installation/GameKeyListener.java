package com.crschnick.pdxu.app.installation;

import com.crschnick.pdxu.app.core.TaskExecutor;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.slf4j.LoggerFactory;

public class GameKeyListener implements NativeKeyListener {

    private final GameApp handle;
    private final long millisecondsGap;
    private long lastCheck;
    private int lastAction;

    public GameKeyListener(GameApp handle, long millisecondsGap) {
        this.handle = handle;
        this.millisecondsGap = millisecondsGap;
    }

    public void nativeKeyPressed(NativeKeyEvent e) {
        if ((e.getModifiers() & NativeKeyEvent.SHIFT_MASK) != 0 && ((e.getModifiers() & NativeKeyEvent.CTRL_MASK) != 0 || (e.getModifiers() & NativeKeyEvent.META_MASK) != 0)) {
            if (e.getKeyCode() == NativeKeyEvent.VC_K && canPass(1)) {
                LoggerFactory.getLogger(GameKeyListener.class).debug("Kill key pressed");
                handle.kill();
            }
            if (e.getKeyCode() == NativeKeyEvent.VC_I && canPass(2)) {
                LoggerFactory.getLogger(GameKeyListener.class).debug("Import key pressed");
                GameAppManager.getInstance().playImportSound();
                GameAppManager.getInstance().importLatest();
            }
            if (e.getKeyCode() == NativeKeyEvent.VC_C && canPass(3)) {
                LoggerFactory.getLogger(GameKeyListener.class).debug("Checkpoint key pressed");
                GameAppManager.getInstance().loadLatestCheckpoint();
            }
            if (e.getKeyCode() == NativeKeyEvent.VC_R && canPass(4)) {
                TaskExecutor.getInstance().submitTask(() -> {
                    LoggerFactory.getLogger(GameKeyListener.class).debug("Reverting to latest save");
                    GameAppManager.getInstance().importLatestAndLaunch();
                }, true);
            }
        }
    }


    private boolean canPass(int action) {
        if (lastCheck == 0 || action != lastAction) {
            lastAction = action;
            lastCheck = System.currentTimeMillis();
            return true;
        }

        var diff = System.currentTimeMillis() - lastCheck;
        if (diff > millisecondsGap) {
            lastCheck = System.currentTimeMillis();
        }
        return diff > millisecondsGap;
    }

    public void nativeKeyReleased(NativeKeyEvent e) {
    }

    public void nativeKeyTyped(NativeKeyEvent e) {
    }
}

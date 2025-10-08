package com.crschnick.pdxu.app.installation;

import com.crschnick.pdxu.app.core.TaskExecutor;
import com.crschnick.pdxu.app.issue.TrackEvent;
import com.crschnick.pdxu.app.savegame.SavegameActions;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

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
                TrackEvent.debug("Kill key pressed");
                GameAppManager.getInstance().killGame(handle);
            }
            if (e.getKeyCode() == NativeKeyEvent.VC_I && canPass(2)) {
                TrackEvent.debug("Import key pressed");
                GameAppManager.getInstance().playImportSound();
                GameAppManager.getInstance().importLatest();
            }
            if (e.getKeyCode() == NativeKeyEvent.VC_C && canPass(3)) {
                TrackEvent.debug("Checkpoint key pressed");
                GameAppManager.getInstance().loadLatestCheckpoint();
            }
            if (e.getKeyCode() == NativeKeyEvent.VC_R && canPass(4)) {
                TrackEvent.debug("Reverting to latest save");
                var g = handle.getGame();
                if (g == null) {
                    return;
                }

                if (!handle.isAlive()) {
                    return;
                }

                if (!GameInstallation.ALL.get(g).getDist().supportsDirectLaunch()) {
                    return;
                }

                if (g.isEnabled()) {
                    TrackEvent.info("Import latest savegame and launch");
                    GameAppManager.getInstance().killGame(handle);
                    TaskExecutor.getInstance().submitTask(() -> {
                        SavegameActions.importLatestAndLaunch(g);
                    }, true);
                }
            }
        }
    }


    private synchronized boolean canPass(int action) {
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

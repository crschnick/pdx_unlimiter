package com.crschnick.pdx_unlimiter.app.game;

import com.crschnick.pdx_unlimiter.app.savegame.FileImporter;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.slf4j.LoggerFactory;

import java.awt.*;

public class GameKeyListener implements NativeKeyListener {

    private GameApp handle;

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
                FileImporter.importLatestSavegame();
                Toolkit.getDefaultToolkit().beep();
            }
        }
    }

    public void nativeKeyReleased(NativeKeyEvent e) {
    }

    public void nativeKeyTyped(NativeKeyEvent e) {
    }
}

package com.crschnick.pdx_unlimiter.app.game;

import com.crschnick.pdx_unlimiter.app.installation.PdxApp;
import org.jnativehook.GlobalScreen;
import org.jnativehook.keyboard.NativeKeyListener;

public class Eu4App extends PdxApp {

    private NativeKeyListener listener;

    public Eu4App(ProcessHandle process) {
        super(process, Type.EU4);
    }

    public void onStart() {
        listener = new Eu4KeyListener(this);
        GlobalScreen.addNativeKeyListener(listener);
    }

    public void onShutdown() {
        GlobalScreen.removeNativeKeyListener(listener);
        listener = null;
    }
}

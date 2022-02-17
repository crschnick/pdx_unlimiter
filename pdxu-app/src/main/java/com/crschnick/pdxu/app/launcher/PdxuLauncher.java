package com.crschnick.pdxu.app.launcher;

import com.crschnick.pdxu.app.installation.Game;

import java.io.IOException;

public class PdxuLauncher extends SupportedLauncher {

    @Override
    public boolean isSupported(Game game) {
        return true;
    }

    @Override
    public void start(Game game) throws IOException {

    }

    @Override
    public String getDisplayName() {
        return "PdxU Launcher";
    }
}

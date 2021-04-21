package com.crschnick.pdx_unlimiter.app.installation.dist;

import com.crschnick.pdx_unlimiter.app.installation.Game;

import java.nio.file.Path;

public class NoLauncherDist extends GameDistType {

    public NoLauncherDist(Game game, Path installLocation) {
        super(game, "No Launcher", installLocation);
    }

    @Override
    public boolean supportsLauncher() {
        return false;
    }

    @Override
    public boolean supportsDirectLaunch() {
        return true;
    }
}

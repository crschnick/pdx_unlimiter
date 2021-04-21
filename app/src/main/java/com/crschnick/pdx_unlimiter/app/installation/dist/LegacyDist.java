package com.crschnick.pdx_unlimiter.app.installation.dist;

import com.crschnick.pdx_unlimiter.app.installation.Game;

import java.io.IOException;
import java.nio.file.Path;

public class LegacyDist extends GameDistType {

    public LegacyDist(Game game, Path installLocation) {
        super(game, "Legacy", installLocation);
    }

    @Override
    public boolean directLaunch() {
        return super.directLaunch();
    }

    @Override
    public void startLauncher() throws IOException {
        super.startLauncher();
    }

    @Override
    public boolean supportsLauncher() {
        return true;
    }

    @Override
    public boolean supportsDirectLaunch() {
        return true;
    }
}

package com.crschnick.pdx_unlimiter.app.installation.dist;

import com.crschnick.pdx_unlimiter.app.installation.Game;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class LegacyLauncherDist extends GameDist {

    public static Optional<GameDist> getDist(Game g, Path dir) {
        if (dir == null) {
            return Optional.empty();
        }

        var legacyLauncher = g.getInstallType().getLegacyLauncherExecutable(dir);
        if (legacyLauncher.isEmpty() || !Files.exists(legacyLauncher.get())) {
            return Optional.empty();
        }

        return Optional.of(new LegacyLauncherDist(g, dir));
    }

    public LegacyLauncherDist(Game game, Path installLocation) {
        super(game, "Legacy", installLocation);
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

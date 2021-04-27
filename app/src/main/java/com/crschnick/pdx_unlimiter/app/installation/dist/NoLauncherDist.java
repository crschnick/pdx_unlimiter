package com.crschnick.pdx_unlimiter.app.installation.dist;

import com.crschnick.pdx_unlimiter.app.installation.Game;

import java.nio.file.Path;
import java.util.Optional;

public class NoLauncherDist extends GameDist {

    public static Optional<GameDist> getDist(Game g, Path dir) {
        if (dir == null) {
            return Optional.empty();
        }

        return Optional.of(new NoLauncherDist(g, dir));
    }

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

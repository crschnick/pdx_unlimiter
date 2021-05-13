package com.crschnick.pdxu.app.installation.dist;

import com.crschnick.pdxu.app.installation.Game;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class NoLauncherDist extends GameDist {

    public NoLauncherDist(Game game, Path installLocation) {
        super(game, "No Launcher", installLocation);
    }

    public static Optional<GameDist> getDist(Game g, Path dir) {
        if (dir == null) {
            return Optional.empty();
        }

        if (!Files.exists(g.getInstallType().getExecutable(dir))) {
            return Optional.empty();
        }

        return Optional.of(new NoLauncherDist(g, dir));
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

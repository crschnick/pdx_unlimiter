package com.crschnick.pdxu.app.installation.dist;

import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.app.util.SupportedOs;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class ProtonDist extends GameDist {

    public static Optional<GameDist> getDist(Game g, Path dir) {
        if (dir == null) {
            return Optional.empty();
        }

        if (SupportedOs.get() != SupportedOs.LINUX) {
            return Optional.empty();
        }

        if (!Files.exists(g.getInstallType().getProtonExecutable(dir)) ){
            return Optional.empty();
        }
        return Optional.of(new ProtonDist(g, "Proton", dir));
    }

    public ProtonDist(Game game, String name, Path installLocation) {
        super(game, name, installLocation);
    }


    public Path getExecutable() {
        return getGame().getInstallType().getProtonExecutable(getInstallLocation());
    }

    @Override
    public boolean isGameInstance(String cmd) {
        return cmd.endsWith(getGame().getInstallType().getProtonExecutableName());
    }

    @Override
    public boolean supportsLauncher() {
        return true;
    }

    @Override
    public boolean supportsDirectLaunch() {
        return false;
    }
}

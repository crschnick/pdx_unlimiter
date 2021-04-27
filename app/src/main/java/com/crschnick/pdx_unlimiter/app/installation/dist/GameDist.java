package com.crschnick.pdx_unlimiter.app.installation.dist;

import com.crschnick.pdx_unlimiter.app.installation.Game;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public abstract class GameDist {

    private final Game game;
    private final String name;
    private final Path installLocation;

    public GameDist(Game game, String name, Path installLocation) {
        this.game = game;
        this.name = name;
        this.installLocation = installLocation;
    }

    public void toNode(ObjectNode node) {
        node.put("location", getInstallLocation().toString());
    }

    public Path getInstallLocation() {
        return installLocation;
    }

    public abstract boolean supportsLauncher();

    public abstract boolean supportsDirectLaunch();

    public boolean directLaunch() {
        throw new UnsupportedOperationException();
    }

    public void startLauncher() throws IOException {
        throw new UnsupportedOperationException();
    }

    public List<Path> getAdditionalSavegamePaths() {
        return List.of();
    }

    public String getName() {
        return name;
    }

    public Game getGame() {
        return game;
    }
}

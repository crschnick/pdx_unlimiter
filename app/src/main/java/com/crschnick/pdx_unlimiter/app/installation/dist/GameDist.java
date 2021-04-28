package com.crschnick.pdx_unlimiter.app.installation.dist;

import com.crschnick.pdx_unlimiter.app.installation.Game;
import com.crschnick.pdx_unlimiter.app.util.OsHelper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.SystemUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    public Optional<String> determineVersion() throws IOException {
        return Optional.empty();
    }

    public Path determineUserDir() throws IOException {
        return OsHelper.getUserDocumentsPath().resolve("Paradox Interactive").resolve(getGame().getFullName());
    }

    public abstract boolean supportsLauncher();

    public abstract boolean supportsDirectLaunch();

    public final void startDirectly(Path executable, List<String> args) throws IOException {
        if (supportsDirectLaunch()) {
            var input = new ArrayList<String>();
            if (SystemUtils.IS_OS_WINDOWS) {
                input.add("cmd");
                input.add("/C");
            }
            input.add(executable.toString());
            input.addAll(args);
            new ProcessBuilder().command(input).start();
        } else {
            throw new UnsupportedOperationException();
        }
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

package com.crschnick.pdxu.app.installation.dist;

import com.crschnick.pdxu.app.installation.Game;
import org.apache.commons.lang3.SystemUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public abstract class GameDist {

    private final Game game;
    private final String name;
    private final Path installLocation;

    public GameDist(Game game, String name, Path installLocation) {
        this.game = Objects.requireNonNull(game);
        this.name = Objects.requireNonNull(name);
        this.installLocation = Objects.requireNonNull(installLocation);
    }

    @Override
    public String toString() {
        return game.getInstallationName() + " (" + name + ") at " + installLocation.toString();
    }

    public Path getInstallLocation() {
        return installLocation;
    }

    public Optional<String> determineVersion() throws IOException {
        return Optional.empty();
    }

    public Path determineUserDir() throws IOException {
        return getGame().getInstallType().determineUserDir(getInstallLocation(), getGame().getInstallationName());
    }

    public Optional<ProcessHandle> getGameInstance(List<ProcessHandle> processes) {
        Optional<ProcessHandle> process = Optional.empty();
        for (var ph : processes) {
            var cmd = ph.info().command().orElse(null);
            if (cmd != null) {
                if (cmd.contains(getExecutable().toString())) {
                    process = Optional.of(ph);
                    break;
                }
            }
        }
        return process;
    }

    public Path getIcon() {
        return getGame().getInstallType().getIcon(getInstallLocation());
    }


    public Path getExecutable() {
        return getGame().getInstallType().getExecutable(getInstallLocation());
    }

    public Optional<Path> getWorkshopDir() {
        return Optional.empty();
    }

    public abstract boolean supportsLauncher();

    public abstract boolean supportsDirectLaunch();

    public void startDirectly(Path executable, List<String> args, Map<String, String> env) throws IOException {
        if (supportsDirectLaunch()) {
            var input = new ArrayList<String>();
            // Make UAC popup if needed
            if (SystemUtils.IS_OS_WINDOWS) {
                input.add("cmd");
                input.add("/C");
            }
            input.add(executable.toString());
            input.addAll(args);
            var pb = new ProcessBuilder().command(input);
            pb.environment().putAll(env);
            pb.start();
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public void startLauncher(Map<String, String> env) throws IOException {
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

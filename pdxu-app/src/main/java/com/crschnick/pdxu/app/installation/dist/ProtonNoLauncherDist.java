package com.crschnick.pdxu.app.installation.dist;

import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.app.util.LocalExec;
import com.crschnick.pdxu.app.util.OsType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;

public class ProtonNoLauncherDist extends GameDist {

    public static Optional<GameDist> getDist(Game g, Path dir) {
        if (dir == null) {
            return Optional.empty();
        }

        if (OsType.ofLocal() != OsType.LINUX) {
            return Optional.empty();
        }

        if (!Files.exists(g.getInstallType().getProtonExecutable(dir))) {
            return Optional.empty();
        }

        var dist = new ProtonNoLauncherDist(g, "Proton No Launcher", dir);
        return Optional.of(dist);
    }

    public ProtonNoLauncherDist(Game game, String name, Path installLocation) {
        super(game, name, installLocation);
    }

    private Path getProtonExecutable() throws IOException {
        for (Path steamLibraryPath : SteamDist.getSteamCommonLibraryPaths()) {
            try (var stream = Files.list(steamLibraryPath)) {
                var l = stream.max(Comparator.comparing(path -> {
                    try {
                        return Files.getLastModifiedTime(path).toInstant();
                    } catch (IOException e) {
                        return Instant.MIN;
                    }
                }));
                if (l.isPresent()) {
                    return l.get();
                }
            }
        }

        throw new IllegalArgumentException("Unable to find proton installation in Steam library");
    }

    @Override
    public void startDirectly(Path executable, List<String> args, Map<String, String> env) throws IOException {
        var input = new ArrayList<String>();
        input.add(getProtonExecutable().toString());
        input.add("run");
        input.add(executable.toString());
        input.addAll(args);
        var pb = new ProcessBuilder().command(input);
        pb.environment().putAll(env);
        var steamDir = SteamDist.getSteamPath().orElseThrow();
        pb.environment().put("STEAM_COMPAT_CLIENT_INSTALL_PATH", steamDir.toString());
        pb.environment().put("STEAM_COMPAT_DATA_PATH", steamDir
                .resolve("steamapps")
                .resolve("compatdata")
                .resolve(String.valueOf(getGame().getSteamAppId())).toString());
        pb.start();
    }

    @Override
    public Path determineUserDir() throws IOException {
        return SteamDist.getSteamPath()
                .orElseThrow()
                .resolve("steamapps")
                .resolve("compatdata")
                .resolve(String.valueOf(getGame().getSteamAppId()))
                .resolve("pfx")
                .resolve("drive_c")
                .resolve("users")
                .resolve("steamuser")
                .resolve("Documents")
                .resolve("Paradox Interactive")
                .resolve(getGame().getInstallationName());
    }

    public Path getExecutable() {
        return getGame().getInstallType().getProtonExecutable(getInstallLocation());
    }

    @Override
    public Optional<ProcessHandle> getGameInstance(List<ProcessHandle> processes) {
        try {
            var running = LocalExec.readStdoutIfPossible("pgrep", getGame().getInstallType().getProtonExecutableName());

            if (getGame() == Game.EU5 && running.isEmpty()) {
                running = LocalExec.readStdoutIfPossible("pgrep", "MainThread");
            }

            if (running.isPresent()) {
                var pid = running.get();
                if (!pid.isEmpty()) {
                    return ProcessHandle.of(Long.parseLong(pid));
                }
            }

            return Optional.empty();
        } catch (Exception ex) {
            return Optional.empty();
        }
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

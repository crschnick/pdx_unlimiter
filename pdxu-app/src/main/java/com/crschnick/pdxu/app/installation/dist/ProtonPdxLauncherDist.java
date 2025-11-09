package com.crschnick.pdxu.app.installation.dist;

import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.app.util.OsType;

import org.apache.commons.lang3.SystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;

public class ProtonPdxLauncherDist extends PdxLauncherDist {

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

        var dist = new ProtonPdxLauncherDist(g, "Proton Paradox Launcher", dir);
        if (!Files.exists(dist.getLauncherSettings())) {
            return Optional.empty();
        }

        return Optional.of(dist);
    }

    public ProtonPdxLauncherDist(Game game, String name, Path installLocation) {
        super(game, name, installLocation);
    }

    public Path getExecutable() {
        return getGame().getInstallType().getProtonExecutable(getInstallLocation());
    }

    @Override
    public Optional<ProcessHandle> getGameInstance(List<ProcessHandle> processes) {
        try {
            var pgrep = new ProcessBuilder("pgrep", getGame().getInstallType().getProtonExecutableName())
                    .redirectError(ProcessBuilder.Redirect.DISCARD)
                    .start();
            var id = pgrep.inputReader().readLine();
            pgrep.waitFor();
            return id != null && !id.trim().isEmpty() ? ProcessHandle.of(Long.parseLong(id.trim())) : Optional.empty();
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    protected Path replaceVariablesInPath(String value) {
        if (SystemUtils.IS_OS_LINUX) {
            value = value.replace(
                    "%USER_DOCUMENTS%",
                    SteamDist.getSteamPath()
                            .orElseThrow()
                            .resolve("steamapps")
                            .resolve("compatdata")
                            .resolve(String.valueOf(getGame().getSteamAppId()))
                            .resolve("pfx")
                            .resolve("drive_c")
                            .resolve("users")
                            .resolve("steamuser")
                            .resolve("Documents")
                            .toString());
        }
        return Path.of(value);
    }

    private Path getProtonExecutable() throws IOException {
        for (Path steamLibraryPath : SteamDist.getSteamLibraryPaths()) {
            var appDir = steamLibraryPath
                    .resolve("steamapps")
                    .resolve("common");
            try (var stream = Files.list(appDir)) {
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
    public boolean supportsLauncher() {
        return true;
    }

    @Override
    public boolean supportsDirectLaunch() {
        return false;
    }
}

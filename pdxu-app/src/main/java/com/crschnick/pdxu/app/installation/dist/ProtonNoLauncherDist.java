package com.crschnick.pdxu.app.installation.dist;

import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.app.util.OsType;
import org.apache.commons.lang3.SystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

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

    @Override
    public boolean supportsLauncher() {
        return false;
    }

    @Override
    public boolean supportsDirectLaunch() {
        return true;
    }
}

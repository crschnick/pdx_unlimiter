package com.crschnick.pdxu.app.installation.dist;

import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.app.util.SupportedOs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class ProtonDist extends GameDist {

    public static Optional<GameDist> getDist(Game g, Path dir) {
        if (dir == null) {
            return Optional.empty();
        }

        if (SupportedOs.get() != SupportedOs.LINUX) {
            return Optional.empty();
        }

        if (!Files.exists(g.getInstallType().getProtonExecutable(dir))) {
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
    public Optional<ProcessHandle> getGameInstance(List<ProcessHandle> processes) {
        try {
            var pgrep = new ProcessBuilder("pgrep", getGame().getInstallType().getProtonExecutableName()).redirectError(
                    ProcessBuilder.Redirect.DISCARD).start();
            var id = pgrep.inputReader().readLine();
            pgrep.waitFor();
            return id != null && !id.trim().isEmpty() ? ProcessHandle.of(Long.parseLong(id.trim())) : Optional.empty();
        } catch (Exception ex) {
            return Optional.empty();
        }
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

    @Override
    public boolean supportsLauncher() {
        return true;
    }

    @Override
    public boolean supportsDirectLaunch() {
        return false;
    }
}

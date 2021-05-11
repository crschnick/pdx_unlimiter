package com.crschnick.pdx_unlimiter.app.installation.dist;

import com.crschnick.pdx_unlimiter.app.core.settings.Settings;
import com.crschnick.pdx_unlimiter.app.gui.dialog.GuiErrorReporter;
import com.crschnick.pdx_unlimiter.app.installation.Game;
import com.crschnick.pdx_unlimiter.app.util.integration.SteamHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SteamDist extends GameDist {

    public static Optional<GameDist> getDist(Game g, Path dir) {
        var installDir = dir;
        if (dir == null) {
            Optional<Path> steamDir = SteamHelper.getSteamPath();
            installDir = steamDir.map(d -> d.resolve("steamapps")
                    .resolve("common").resolve(g.getFullName()))
                    .filter(Files::exists)
                    .orElse(null);
        }
        if (installDir != null) {
            var steamFile = g.getInstallType().getSteamSpecificFile(installDir);
            if (Files.exists(steamFile)) {
                var basicDist = GameDists.getBasicDistFromDirectory(g, installDir);
                if (basicDist.isPresent()) {
                    return Optional.of(new SteamDist(g, installDir, basicDist.get()));
                }
            }
        }
        return Optional.empty();
    }

    private final GameDist dist;

    public SteamDist(Game g, Path installLocation, GameDist dist) {
        super(g, "Steam", installLocation);
        this.dist = dist;
    }

    @Override
    public Path determineUserDir() throws IOException {
        return dist.determineUserDir();
    }

    @Override
    public Optional<String> determineVersion() throws IOException {
        return dist.determineVersion();
    }

    @Override
    public String getName() {
        return "Steam + " + dist.getName();
    }

    public boolean directLaunch() {
        if (!SteamHelper.isSteamRunning() && Settings.getInstance().startSteam.getValue()) {
            GuiErrorReporter.showSimpleErrorMessage("Steam is not started but required.\n" +
                    "Please start Steam first before launching the game");
            return false;
        }

        return false;
    }

    @Override
    public boolean supportsLauncher() {
        return true;
    }

    @Override
    public boolean supportsDirectLaunch() {
        return dist.supportsDirectLaunch();
    }

    @Override
    public void startLauncher() throws IOException {
        if (Settings.getInstance().startSteam.getValue()) {
            SteamHelper.openSteamURI("steam://run/" + getGame().getSteamAppId() + "//");
        } else {
            super.startLauncher();
        }
    }

    @Override
    public List<Path> getAdditionalSavegamePaths() {
        return SteamHelper.getRemoteDataPaths(getGame().getSteamAppId()).stream()
                .map(d -> d.resolve("save games"))
                .collect(Collectors.toList());
    }
}

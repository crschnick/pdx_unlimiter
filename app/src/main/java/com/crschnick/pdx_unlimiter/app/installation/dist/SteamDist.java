package com.crschnick.pdx_unlimiter.app.installation.dist;

import com.crschnick.pdx_unlimiter.app.core.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.core.settings.Settings;
import com.crschnick.pdx_unlimiter.app.gui.dialog.GuiErrorReporter;
import com.crschnick.pdx_unlimiter.app.installation.Game;
import com.crschnick.pdx_unlimiter.app.installation.GameInstallation;
import com.crschnick.pdx_unlimiter.app.util.integration.SteamHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SteamDist extends PdxLauncherDist {

    public static Optional<SteamDist> getDist(Game g) {
        Optional<Path> steamDir = SteamHelper.getSteamPath();
        var installDir = steamDir.map(d -> d.resolve("steamapps")
                .resolve("common").resolve(g.getFullName()))
                .filter(Files::exists);
        if (installDir.isPresent()) {
            try {
                return Optional.of(installDir.get().toRealPath());
            } catch (IOException e) {
                ErrorHandler.handleException(e);
            }
        }
        return Optional.empty();
    }

    private final int appId;

    public SteamDist(Game g, Path installLocation, int appId) {
        super(g, "Steam", installLocation);
        this.appId = appId;
    }

    @Override
    public boolean checkDirectLaunch() {
        if (!SteamHelper.isSteamRunning() && Settings.getInstance().startSteam.getValue()) {
            GuiErrorReporter.showSimpleErrorMessage("Steam is not started but required.\n" +
                    "Please start Steam first before launching the game");
            return false;
        }

        return true;
    }

    @Override
    public void startLauncher() throws IOException {
        if (Settings.getInstance().startSteam.getValue()) {
            SteamHelper.openSteamURI("steam://run/" + appId + "//");
        } else {
            super.startLauncher();
        }
    }

    @Override
    public List<Path> getAdditionalSavegamePaths() {
        return SteamHelper.getRemoteDataPaths(appId).stream()
                .map(d -> d.resolve("save games"))
                .collect(Collectors.toList());
    }
}

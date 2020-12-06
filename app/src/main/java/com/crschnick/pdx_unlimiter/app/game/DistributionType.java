package com.crschnick.pdx_unlimiter.app.game;

import com.crschnick.pdx_unlimiter.app.installation.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.installation.Settings;
import com.crschnick.pdx_unlimiter.app.util.SteamHelper;
import org.apache.commons.lang3.SystemUtils;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public abstract class DistributionType {

    private String name;

    public DistributionType(String name) {
        this.name = name;
    }

    public abstract void launch();

    public abstract List<Path> getSavegamePaths();

    public static class Steam extends DistributionType {

        private int appId;

        public Steam(int appId) {
            super("Steam");
            this.appId = appId;
        }

        @Override
        public void launch() {
            try {
                Desktop.getDesktop().browse(new URI("steam://run/" + appId + "//"));
            } catch (IOException | URISyntaxException e) {
                ErrorHandler.handleException(e);
            }
        }

        @Override
        public List<Path> getSavegamePaths() {
            return SteamHelper.getRemoteDataPaths(appId).stream()
                    .map(d -> d.resolve("save games"))
                    .collect(Collectors.toList());
        }
    }

    public static class PdxLauncher extends DistributionType {

        private Path launcherPath;

        public PdxLauncher(Path launcherPath) {
            super("Paradox Launcher");
            this.launcherPath = launcherPath;
        }

        @Override
        public void launch() {
            Path bootstrapper = null;
            if (SystemUtils.IS_OS_WINDOWS) {
                bootstrapper = Path.of(System.getenv("LOCALAPPDATA"))
                        .resolve("Programs")
                        .resolve("Paradox Interactive")
                        .resolve("bootstrapper-v2.exe");
            } else if (SystemUtils.IS_OS_LINUX) {
            }

            try {
                new ProcessBuilder()
                        .directory(launcherPath.toFile())
                        .command(bootstrapper.toString(),
                                "--pdxlGameDir", launcherPath.toString(),
                                "--gameDir", launcherPath.toString())
                        .start();
            } catch (IOException e) {
                ErrorHandler.handleException(e);
            }
        }

        @Override
        public List<Path> getSavegamePaths() {
            return List.of();
        }
    }
}

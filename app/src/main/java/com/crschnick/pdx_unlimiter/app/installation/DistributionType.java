package com.crschnick.pdx_unlimiter.app.installation;

import com.crschnick.pdx_unlimiter.app.core.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.util.SteamHelper;
import org.apache.commons.lang3.SystemUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public abstract class DistributionType {

    private final String name;

    public DistributionType(String name) {
        this.name = name;
    }

    public abstract void launch();

    public abstract List<Path> getSavegamePaths();

    public static class Steam extends DistributionType {

        private final int appId;

        public Steam(int appId) {
            super("Steam");
            this.appId = appId;
        }

        @Override
        public void launch() {
            SteamHelper.openSteamURI("steam://run/" + appId + "//");
        }

        @Override
        public List<Path> getSavegamePaths() {
            return SteamHelper.getRemoteDataPaths(appId).stream()
                    .map(d -> d.resolve("save games"))
                    .collect(Collectors.toList());
        }
    }

    public static class PdxLauncher extends DistributionType {

        private final Path launcherPath;

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
                bootstrapper = Path.of(System.getProperty("user.home"))
                        .resolve(".paradoxlauncher")
                        .resolve("bootstrapper-v2");
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

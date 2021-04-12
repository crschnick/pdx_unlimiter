package com.crschnick.pdx_unlimiter.app.installation;

import com.crschnick.pdx_unlimiter.app.core.settings.Settings;
import com.crschnick.pdx_unlimiter.app.gui.dialog.GuiErrorReporter;
import com.crschnick.pdx_unlimiter.app.util.integration.SteamHelper;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public abstract class GameDistributionType {

    protected final GameInstallation installation;
    private final String name;

    public GameDistributionType(String name, GameInstallation installation) {
        this.name = name;
        this.installation = installation;
    }

    private static Path getBootstrapper() throws IOException {
        Path bootstrapper;
        if (SystemUtils.IS_OS_WINDOWS) {
            bootstrapper = Path.of(System.getenv("LOCALAPPDATA"))
                    .resolve("Programs")
                    .resolve("Paradox Interactive")
                    .resolve("bootstrapper-v2.exe");
        } else {
            bootstrapper = Path.of(System.getProperty("user.home"))
                    .resolve(".paradoxlauncher")
                    .resolve("bootstrapper-v2");
        }

        if (!Files.exists(bootstrapper)) {
            try {
                LoggerFactory.getLogger(GameDistributionType.class).error("Pdx-Dir: " +
                        Files.list(bootstrapper.getParent())
                                .map(Path::toString)
                                .collect(Collectors.joining(", ")));
            } catch (Exception ignored) {}

            throw new IOException("Paradox Launcher bootstrapper not found.\n" +
                    "Please try to enable 'Start through Steam' in the settings menu.\n" +
                    "If you don't use Steam or believe that this an error, please report it.");
        }

        return bootstrapper;
    }

    private static void startParadoxLauncher(Path launcherPath) throws IOException {
        Path bootstrapper = getBootstrapper();

        new ProcessBuilder()
                .directory(launcherPath.toFile())
                .command(bootstrapper.toString(),
                        "--pdxlGameDir", launcherPath.toString(),
                        "--gameDir", launcherPath.toString())
                .start();
    }

    public abstract boolean checkDirectLaunch();

    public abstract void startLauncher() throws IOException;

    public abstract List<Path> getSavegamePaths();

    public String getName() {
        return name;
    }

    public static class Steam extends GameDistributionType {

        private final int appId;

        public Steam(int appId, GameInstallation installation) {
            super("Steam", installation);
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
                GameDistributionType.startParadoxLauncher(installation.getLauncherDataPath());
            }
        }

        @Override
        public List<Path> getSavegamePaths() {
            return SteamHelper.getRemoteDataPaths(appId).stream()
                    .map(d -> d.resolve("save games"))
                    .collect(Collectors.toList());
        }
    }

    public static class PdxLauncher extends GameDistributionType {

        public PdxLauncher(GameInstallation installation) {
            super("Paradox Launcher", installation);
        }

        @Override
        public boolean checkDirectLaunch() {
            return true;
        }

        @Override
        public void startLauncher() throws IOException {
            GameDistributionType.startParadoxLauncher(installation.getLauncherDataPath());
        }

        @Override
        public List<Path> getSavegamePaths() {
            return List.of();
        }
    }
}

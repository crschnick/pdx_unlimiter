package com.crschnick.pdx_unlimiter.app.installation.dist;

import com.crschnick.pdx_unlimiter.app.installation.Game;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PdxLauncherDist extends GameDistType {

    public static Optional<GameDistType> getDist(Game g, JsonNode node) {
        if (!node.has("location")) {
            return Optional.empty();
        }

        var loc = Path.of(node.get("location").textValue());
        return Optional.of(new PdxLauncherDist(g, "Paradox Launcher", loc));
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
                LoggerFactory.getLogger(GameDistType.class).error("Pdx-Dir: " +
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

    public PdxLauncherDist(Game g, String name, Path installLocation) {
        super(g, name, installLocation);
    }

    @Override
    public boolean supportsLauncher() {
        return true;
    }

    @Override
    public boolean supportsDirectLaunch() {
        return true;
    }

    @Override
    public void startLauncher() throws IOException {
        startParadoxLauncher(getGame().getInstallType().getLauncherDataPath(getInstallLocation()));
    }

    @Override
    public List<Path> getAdditionalSavegamePaths() {
        return List.of();
    }
}

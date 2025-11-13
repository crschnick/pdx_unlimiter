package com.crschnick.pdxu.app.installation.dist;

import com.crschnick.pdxu.app.core.AppSystemInfo;
import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.app.util.FileSystemHelper;
import com.crschnick.pdxu.app.util.JacksonMapper;
import com.crschnick.pdxu.app.util.OsType;
import com.crschnick.pdxu.app.util.WindowsRegistry;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.SystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PdxLauncherDist extends GameDist {

    public PdxLauncherDist(Game g, String name, Path installLocation) {
        super(g, name, installLocation);
    }

    public static Optional<GameDist> getDist(Game g, Path dir) {
        if (dir == null) {
            return Optional.empty();
        }

        if (!Files.exists(g.getInstallType().getLauncherDataPath(dir).resolve("launcher-settings.json"))) {
            return Optional.empty();
        }

        if (getBootstrapper().isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new PdxLauncherDist(g, "Paradox Launcher", dir));
    }

    private static Optional<Path> getParadoxLauncherPath() {
        Optional<String> launcherDir = Optional.empty();
        switch (OsType.ofLocal()) {
            case OsType.Linux ignored -> {
                var home = Path.of(System.getProperty("user.home"))
                        .resolve(".paradoxlauncher");
                if (Files.isDirectory(home)) {
                    launcherDir = Optional.of(home.toString());
                } else {
                    var flatpak = AppSystemInfo.ofCurrent().getUserHome().resolve(".var/app/com.valvesoftware.Steam/.paradoxlauncher");
                    if (Files.isDirectory(flatpak)) {
                        launcherDir = Optional.of(flatpak.toString());
                    }
                }
            }
            case OsType.MacOs ignored -> {
                // The launcher is in Application Support
                String s = AppSystemInfo.ofMacOs().getUserHome().resolve("Library", "Application Support")
                        .resolve("Paradox Interactive")
                        .toString();
                launcherDir = Optional.ofNullable(Files.isDirectory(Path.of(s)) ? s : null);
            }
            case OsType.Windows ignored -> {
                launcherDir = WindowsRegistry.of()
                        .readStringValueIfPresent(
                                WindowsRegistry.HKEY_CURRENT_USER,
                                "SOFTWARE\\Paradox Interactive\\Paradox Launcher v2",
                                "LauncherInstallation");
            }
        }
        return launcherDir.map(Path::of);
    }

    private static Optional<Path> getBootstrapper() {
        var dir = getParadoxLauncherPath();
        return dir.map(p -> p.resolve("bootstrapper-v2" + (SystemUtils.IS_OS_WINDOWS ? ".exe" : "")))
                .filter(Files::exists);
    }

    private static void startParadoxLauncher(Path launcherPath, Map<String, String> env) throws IOException {
        var bootstrapper = getBootstrapper();
        if (bootstrapper.isEmpty()) {
            return;
        }

        var pb = new ProcessBuilder().directory(launcherPath.toFile());

        var cmd = new ArrayList<>(List.of(
                bootstrapper.get().toString(),
                "--pdxlGameDir",
                launcherPath.toString(),
                "--gameDir",
                launcherPath.toString()));
        if (SystemUtils.IS_OS_WINDOWS) {
            cmd.add(0, "cmd.exe");
            cmd.add(1, "/c");
        }

        pb.command(cmd);
        pb.environment().putAll(env);
        pb.start();
    }

    protected Path getLauncherSettings() {
        return getGame()
                .getInstallType()
                .getLauncherDataPath(getInstallLocation())
                .resolve("launcher-settings.json");
    }

    protected Path replaceVariablesInPath(String value) {
        switch (OsType.ofLocal()) {
            case OsType.Linux ignored -> {
                // Ugly fix for flatpak
                var isFlatpak = getInstallLocation().toString().contains("com.valvesoftware.Steam");
                if (isFlatpak) {
                    var flatpak = Path.of(System.getProperty("user.home"), ".var/app/com.valvesoftware.Steam/.local/share");
                    value = value.replace(
                            "$LINUX_DATA_HOME",
                            flatpak.toString());
                } else {
                    value = value.replace(
                            "$LINUX_DATA_HOME",
                            FileSystemHelper.getParadoxDocumentsPath().toString());
                }
            }
            case OsType.MacOs ignored -> {
                value = value.replace("~", System.getProperty("user.home"));
            }
            case OsType.Windows ignored -> {
                value = value.replace(
                        "%USER_DOCUMENTS%",
                        FileSystemHelper.getParadoxDocumentsPath().toString());
            }
        }
        return Path.of(value);
    }

    @Override
    public Optional<String> determineVersion() throws IOException {
        JsonNode node =
                JacksonMapper.getDefault().readTree(getLauncherSettings().toFile());
        return Optional.ofNullable(node.get("version")).map(JsonNode::textValue);
    }

    @Override
    public Path determineUserDir() throws IOException {
        JsonNode node =
                JacksonMapper.getDefault().readTree(getLauncherSettings().toFile());
        return Optional.ofNullable(node.get("gameDataPath"))
                .map(JsonNode::textValue)
                .map(this::replaceVariablesInPath)
                .orElse(super.determineUserDir());
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
    public void startLauncher(Map<String, String> env) throws IOException {
        startParadoxLauncher(getGame().getInstallType().getLauncherDataPath(getInstallLocation()), env);
    }

    @Override
    public List<Path> getAdditionalSavegamePaths() {
        return List.of();
    }
}

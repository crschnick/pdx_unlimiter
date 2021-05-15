package com.crschnick.pdxu.app.installation.dist;

import com.crschnick.pdxu.app.core.ErrorHandler;
import com.crschnick.pdxu.app.core.TaskExecutor;
import com.crschnick.pdxu.app.core.settings.Settings;
import com.crschnick.pdxu.app.gui.dialog.GuiErrorReporter;
import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.app.util.ThreadHelper;
import com.crschnick.pdxu.app.util.WindowsRegistry;
import org.apache.commons.lang3.ArchUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SteamDist extends GameDist {

    private final GameDist dist;

    public SteamDist(Game g, Path installLocation, GameDist dist) {
        super(g, "Steam", installLocation);
        this.dist = dist;
    }

    private static Optional<Path> getSteamPath() {
        Optional<String> steamDir = Optional.empty();
        if (SystemUtils.IS_OS_WINDOWS) {
            if (ArchUtils.getProcessor().is64Bit()) {
                steamDir = WindowsRegistry.readRegistry("HKEY_LOCAL_MACHINE\\SOFTWARE\\Wow6432Node\\Valve\\Steam", "InstallPath");
            } else {
                steamDir = WindowsRegistry.readRegistry("HKEY_LOCAL_MACHINE\\SOFTWARE\\Valve\\Steam", "InstallPath");
            }
        } else if (SystemUtils.IS_OS_LINUX) {
            String s = Path.of(System.getProperty("user.home"), ".steam", "steam").toString();
            steamDir = Optional.ofNullable(Files.isDirectory(Path.of(s)) ? s : null);
        }

        return steamDir.map(Path::of);
    }

    private static List<Path> getSteamLibraryPaths() {
        var p = getSteamPath();
        if (p.isEmpty()) {
            return List.of();
        }

        var list = new ArrayList<Path>();
        list.add(p.get().resolve("steamapps").resolve("common"));

        try {
            var libraryFoldersFile = Files.readString(p.get().resolve("steamapps").resolve("libraryfolders.vdf"));
            Pattern pattern = Pattern.compile("\\s+\"\\d+\"\\s+\"(.+)\"");
            libraryFoldersFile.lines().forEach(line -> {
                var m = pattern.matcher(line);
                if (m.find()) {
                    list.add(Path.of(m.group(1)).resolve("steamapps").resolve("common"));
                }
            });
        } catch (Exception e) {
            return list;
        }

        return list;
    }

    public static Optional<GameDist> getDist(Game g, Path dir) {
        if (dir == null) {
            for (var path : getSteamLibraryPaths()) {
                var installDir = path.resolve(g.getFullName());
                var found = getDist(g, installDir);
                if (found.isPresent()) {
                    return found;
                }
            }
        } else {
            var steamFile = g.getInstallType().getSteamSpecificFile(dir);
            if (Files.exists(steamFile)) {
                var basicDist = GameDists.getBasicDistFromDirectory(g, dir);
                if (basicDist.isPresent()) {
                    return Optional.of(new SteamDist(g, dir, basicDist.get()));
                }
            }
        }
        return Optional.empty();
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

    private boolean isSteamRunning() {
        return ProcessHandle.allProcesses()
                .map(p -> p.info().command())
                .flatMap(Optional::stream)
                .anyMatch(c -> c.contains("steam") && c.contains("Steam"));
    }


    private void openSteamURI(String uri) {
        if (SystemUtils.IS_OS_LINUX) {
            if (!isSteamRunning()) {
                GuiErrorReporter.showSimpleErrorMessage("Steam is not started. " +
                        "Please start Steam first before launching the game");
            } else {
                TaskExecutor.getInstance().submitTask(() -> {
                    try {
                        var p = new ProcessBuilder("steam", uri).start();
                        p.getInputStream().readAllBytes();
                    } catch (Exception e) {
                        ErrorHandler.handleException(e);
                    }
                }, true);
            }
        } else {
            ThreadHelper.browse(uri);
        }
    }

    @Override
    public void startDirectly(Path executable, List<String> args) throws IOException {
        if (!isSteamRunning() && Settings.getInstance().startSteam.getValue()) {
            GuiErrorReporter.showSimpleErrorMessage("Steam is not started but required.\n" +
                    "Please start Steam first before launching the game");
            return;
        }

        dist.startDirectly(executable, args);
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
            openSteamURI("steam://run/" + getGame().getSteamAppId() + "//");
        } else {
            super.startLauncher();
        }
    }

    private List<Path> getRemoteDataPaths(int appId) {
        var p = getSteamPath();
        if (p.isEmpty()) {
            return List.of();
        }

        Path userData = p.get().resolve("userdata");
        try {
            return Files.list(userData)
                    .map(d -> d.resolve(String.valueOf(appId)).resolve("remote"))
                    .filter(Files::exists)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return List.of();
        }
    }

    @Override
    public List<Path> getAdditionalSavegamePaths() {
        return getRemoteDataPaths(getGame().getSteamAppId()).stream()
                .map(d -> d.resolve("save games"))
                .collect(Collectors.toList());
    }
}

package com.crschnick.pdxu.app.installation.dist;


import com.crschnick.pdxu.app.core.TaskExecutor;
import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.app.issue.ErrorEventFactory;
import com.crschnick.pdxu.app.util.OsType;
import com.crschnick.pdxu.app.util.ThreadHelper;
import com.crschnick.pdxu.app.util.WindowsRegistry;
import org.apache.commons.lang3.ArchUtils;
import org.apache.commons.lang3.SystemUtils;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SteamDist extends GameDist {

    private final GameDist dist;

    public SteamDist(Game g, Path installLocation, GameDist dist) {
        super(g, "Steam", installLocation);
        this.dist = dist;
    }

    @Override
    public Path getExecutable() {
        return dist.getExecutable();
    }

    @Override
    public Optional<Path> getWorkshopDir() {
        var s = getSteamPath();
        return s.map(p -> getSteamAppsCommonDir(p).getParent().resolve("workshop").resolve("content").resolve(String.valueOf(getGame().getSteamAppId())));
    }

    @Override
    public Optional<ProcessHandle> getGameInstance(List<ProcessHandle> processes) {
        return dist.getGameInstance(processes);
    }

    public static Optional<Path> getSteamPath() {
        Optional<String> steamDir = Optional.empty();
        switch (OsType.ofLocal()) {
            case OsType.Linux ignored -> {
                try {
                    var steamPath = Path.of(System.getProperty("user.home"), ".steam", "steam");
                    if (Files.exists(steamPath)) {
                        // Resolve symlink
                        var steamRealPath = steamPath.toRealPath();
                        steamDir = Optional.ofNullable(Files.isDirectory(steamRealPath) ? steamRealPath.toString() : null);
                    }
                } catch (Exception ex) {
                    ErrorEventFactory.fromThrowable(ex).handle();
                }
            }
            case OsType.MacOs ignored -> {
                var steamPath = Path.of(System.getProperty("user.home"), "Library", "Application Support", "Steam");
                if (Files.exists(steamPath)) {
                    steamDir = Optional.of(steamPath.toString());
                }
            }
            case OsType.Windows ignored -> {
                if (ArchUtils.getProcessor().is64Bit()) {
                    steamDir = WindowsRegistry.of().readStringValueIfPresent(WindowsRegistry.HKEY_LOCAL_MACHINE, "SOFTWARE\\Wow6432Node\\Valve\\Steam", "InstallPath");
                } else {
                    steamDir = WindowsRegistry.of().readStringValueIfPresent(WindowsRegistry.HKEY_LOCAL_MACHINE, "SOFTWARE\\Valve\\Steam", "InstallPath");
                }
            }
        }

        try {
            return steamDir.map(Path::of);
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    private static final Pattern STEAM_LIBRARY_DIR_NEW = Pattern.compile("\\s+\"path\"\\s+\"(.+)\"");

    private static Path getSteamAppsCommonDir(Path base) {
        var common = base.resolve("steamapps").resolve("common");
        try {
            return common.toRealPath();
        } catch (IOException e) {
            return common;
        }
    }

    private static List<Path> getSteamLibraryPaths() {
        var p = getSteamPath();
        if (p.isEmpty()) {
            return List.of();
        }

        var list = new ArrayList<Path>();
        list.add(getSteamAppsCommonDir(p.get()));

        try {
            var libraryFoldersFile = Files.readString(p.get().resolve("steamapps").resolve("libraryfolders.vdf"));
            libraryFoldersFile.lines().forEach(line -> {
                try {
                    var m = STEAM_LIBRARY_DIR_NEW.matcher(line);
                    if (m.find()) {
                        list.add(getSteamAppsCommonDir(Path.of(m.group(1))));
                    }
                } catch (InvalidPathException ignored) {
                }
            });
        } catch (Exception e) {
            return list;
        }

        return list;
    }

    private static boolean isInSteamLibraryDir(Path dir) {
        try {
            dir = dir.toRealPath();
        } catch (IOException ignored) {
        }

        Path finalDir = dir;
        boolean inSteamLibraryDir = getSteamLibraryPaths().stream().anyMatch(ld -> finalDir.startsWith(ld));
        boolean looksLikeSteamLibDir = dir.getNameCount() > 3 && Files.exists(
                dir.getParent().getParent().getParent().resolve("libraryfolder.vdf"));
        return inSteamLibraryDir || looksLikeSteamLibDir;
    }

    public static Optional<GameDist> getDist(Game g, Path dir) {
        if (dir == null) {
            for (var path : getSteamLibraryPaths()) {
                var installDir = path.resolve(g.getInstallationName());
                var found = getDist(g, installDir);
                if (found.isPresent()) {
                    return found;
                }
            }
        } else {
            boolean inSteamLibraryDir = isInSteamLibraryDir(dir);
            var steamFile = g.getInstallType().getSteamSpecificFile(dir);
            if (inSteamLibraryDir && (steamFile == null || Files.exists(steamFile))) {
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
                .anyMatch(c -> c.contains("steam") || c.contains("Steam"));
    }


    private void openSteamURI(String uri) {
        if (SystemUtils.IS_OS_LINUX) {
            if (!isSteamRunning()) {
                ErrorEventFactory.fromMessage("Steam is not started. " +
                        "Please start Steam first before launching the game").handle();
            } else {
                TaskExecutor.getInstance().submitTask(() -> {
                    try {
                        var p = new ProcessBuilder("steam", uri).start();
                        p.getInputStream().readAllBytes();
                    } catch (Exception e) {
                        ErrorEventFactory.fromThrowable(e).handle();
                    }
                }, true);
            }
        } else {
            ThreadHelper.runFailableAsync(() -> {
                Desktop.getDesktop().browse(URI.create(uri));
            });
        }
    }

    @Override
    public void startDirectly(Path executable, List<String> args, Map<String, String> env) throws IOException {
        if (!isSteamRunning()) {
            ErrorEventFactory.fromMessage("Steam is not started but required.\n" +
                    "Please start Steam first before launching the game").handle();
            return;
        }

        var completeEnv = new HashMap<String, String>();
        completeEnv.putAll(env);
        completeEnv.putAll(createSteamEnv());
        dist.startDirectly(executable, args, completeEnv);
    }

    @Override
    public boolean supportsLauncher() {
        return true;
    }

    @Override
    public boolean supportsDirectLaunch() {
        return dist.supportsDirectLaunch();
    }

    private Map<String, String> createSteamEnv() {
        return Map.of(
                "SteamAppId", String.valueOf(getGame().getSteamAppId()),
                "SteamGameId", String.valueOf(getGame().getSteamAppId()),
                "SteamOverlayGameId", String.valueOf(getGame().getSteamAppId())
        );
    }

    @Override
    public void startLauncher(Map<String, String> env) {
        openSteamURI("steam://run/" + getGame().getSteamAppId() + "//");
    }

    private List<Path> getRemoteDataPaths(int appId) {
        var p = getSteamPath();
        if (p.isEmpty()) {
            return List.of();
        }

        Path userData = p.get().resolve("userdata");
        try (var s = Files.list(userData)) {
            return s.map(d -> d.resolve(String.valueOf(appId)).resolve("remote"))
             .filter(Files::exists)
             .toList();
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

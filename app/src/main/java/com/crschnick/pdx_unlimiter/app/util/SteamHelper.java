package com.crschnick.pdx_unlimiter.app.util;

import com.crschnick.pdx_unlimiter.app.gui.dialog.GuiErrorReporter;
import com.crschnick.pdx_unlimiter.app.core.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.core.TaskExecutor;
import org.apache.commons.lang3.ArchUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SteamHelper {

    public static void openSteamURI(String uri) {
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
                }, true, false);
            }
        } else {
            ThreadHelper.browse(uri);
        }
    }

    public static boolean isSteamRunning() {
        return ProcessHandle.allProcesses()
                .map(p -> p.info().command())
                .flatMap(Optional::stream)
                .anyMatch(c -> c.contains("steam") && c.contains("Steam"));
    }

    public static List<Path> getRemoteDataPaths(int appId) {
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

    public static Optional<Path> getSteamPath() {
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
}

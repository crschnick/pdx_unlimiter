package com.crschnick.pdx_unlimiter.app.game;

import com.crschnick.pdx_unlimiter.app.util.WindowsRegistry;
import org.apache.commons.lang3.ArchUtils;
import org.apache.commons.lang3.SystemUtils;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public abstract class GameInstallation {

    public static Eu4Installation EU4 = null;
    private String name;
    private Path path;
    public GameInstallation(String name, Path path) {
        this.name = name;
        this.path = path;
    }

    public static void initInstallations() throws Exception {
        if (EU4 != null) {
            EU4.init();
        }
    }

    public static Optional<Path> getInstallPath(String app) {
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

        if (!steamDir.isPresent()) {
            return Optional.empty();
        }
        Path p = Paths.get(steamDir.get(), "steamapps", "common", app);
        return p.toFile().exists() ? Optional.of(p) : Optional.empty();
    }

    public abstract void start();

    public abstract void init() throws Exception;

    public abstract boolean isValid();

    public String getName() {
        return name;
    }

    public Path getPath() {
        return path;
    }
}

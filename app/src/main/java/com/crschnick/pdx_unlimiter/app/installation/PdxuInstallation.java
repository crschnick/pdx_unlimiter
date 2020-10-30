package com.crschnick.pdx_unlimiter.app.installation;

import io.sentry.Sentry;
import org.apache.commons.io.FileUtils;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Logger;

public class PdxuInstallation {

    private static PdxuInstallation INSTANCE;

    public static boolean init() throws Exception {
        Path appPath = Path.of(System.getProperty("java.home"));
        boolean prod = appPath.toFile().getName().equals("app")
                || appPath.toFile().getName().equals("image");
        String v;
        Path installDir;
        Optional<Path> achievementsLocation = Optional.empty();
        boolean developerMode = false;
        boolean nativeHook = true;

        Properties props = new Properties();
        if (prod) {
            v = Files.readString(appPath.resolve("version"));
            installDir = appPath.getParent();
            props.load(Files.newInputStream(appPath.resolve("pdxu.properties")));
        } else {
            props.load(Files.newInputStream(Path.of("pdxu.properties")));

            v = "dev";
            installDir = Optional.ofNullable(props.get("installDir"))
                    .map(val -> Path.of(val.toString()))
                    .filter(val -> val.isAbsolute() && Files.exists(val))
                    .orElseThrow(() -> new NoSuchElementException("Invalid installDir for dev build"));
        }
        achievementsLocation = Optional.ofNullable(props.get("achievementDir"))
                .map(val -> Path.of(val.toString()))
                .filter(val -> val.isAbsolute() && Files.exists(val));
        developerMode = Optional.ofNullable(props.get("developerMode"))
                .map(val -> Boolean.parseBoolean(val.toString()))
                .orElse(false);
        nativeHook = Optional.ofNullable(props.get("enableJNativeHook"))
                .map(val -> Boolean.parseBoolean(val.toString()))
                .orElse(true);

        INSTANCE = new PdxuInstallation(installDir, v, prod, achievementsLocation, developerMode, nativeHook);

        if (INSTANCE.isAlreadyRunning()) {
            return false;
        }

        return true;
    }

    public static void shutdown() throws Exception {
        INSTANCE.updateLauncher();
    }

    private void updateLauncher() throws Exception {
        if (!INSTANCE.getNewLauncherLocation().toFile().exists()) {
            return;
        }

        FileUtils.deleteDirectory(INSTANCE.getOldLauncherLocation().toFile());
        FileUtils.moveDirectory(INSTANCE.getLauncherLocation().toFile(), INSTANCE.getOldLauncherLocation().toFile());
        FileUtils.moveDirectory(INSTANCE.getNewLauncherLocation().toFile(), INSTANCE.getLauncherLocation().toFile());
    }

    private boolean isAlreadyRunning() {
        return ProcessHandle.allProcesses()
                .map(h -> h.info().command().orElse(""))
                .filter(s -> s.equals(getExecutableLocation().toString()))
                .count() >= 2;
    }

    private Path location;
    private String version;
    private boolean production;
    private Optional<Path> achievementsLocation;
    private boolean developerMode;
    private boolean nativeHookEnabled;

    public PdxuInstallation(Path location, String version, boolean production, Optional<Path> achievementsLocation, boolean developerMode, boolean nativeHookEnabled) {
        this.location = location;
        this.version = version;
        this.production = production;
        this.achievementsLocation = achievementsLocation;
        this.developerMode = developerMode;
        this.nativeHookEnabled = nativeHookEnabled;
    }

    public boolean isNativeHookEnabled() {
        return nativeHookEnabled;
    }

    public Path getLocation() {
        return location;
    }

    public Path getExecutableLocation() {
        return getAppLocation().resolve("bin").resolve("java.exe");
    }

    public Path getLauncherLocation() {
        return location.resolve("launcher");
    }

    public Path getOldLauncherLocation() {
        return location.resolve("launcher_old");
    }

    public Path getNewLauncherLocation() {
        return location.resolve("launcher_new");
    }

    public Path getLogsLocation() {
        return location.resolve("logs");
    }

    public Path getAppLocation() {
        return location.resolve("app");
    }

    public Path getAchievementsLocation() {
        return achievementsLocation.orElse(location.resolve("achievements"));
    }

    public Path getSettingsLocation() {
        return location.resolve("settings");
    }

    public Path getSavegameLocation() {
        return location.resolve("savegames");
    }

    public String getVersion() {
        return version;
    }

    public static PdxuInstallation getInstance() {
        return INSTANCE;
    }

    public boolean isProduction() {
        return production;
    }

    public boolean isDeveloperMode() {
        return developerMode;
    }
}

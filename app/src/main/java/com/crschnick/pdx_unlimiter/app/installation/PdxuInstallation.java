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
import java.util.logging.Logger;

public class PdxuInstallation {

    private static PdxuInstallation INSTANCE;

    public static boolean init() throws Exception {
        Path appPath = Path.of(System.getProperty("java.home"));

        boolean prod = false;
        String v;
        Path installDir;
        if (appPath.toFile().getName().equals("app")) {
            v = Files.readString(appPath.resolve("version"));
            installDir = appPath.getParent();
            prod = true;
        } else {
            v = "dev";
            String dir = Optional.ofNullable(System.getProperty("pdxu.installDir"))
                    .orElseThrow(() -> new NoSuchElementException("Property pdxu.installDir missing for dev build"));
            installDir = Path.of(dir);
        }
        INSTANCE = new PdxuInstallation(installDir, v, prod);

        FileUtils.forceMkdir(INSTANCE.getLogsLocation().toFile());
        if (prod) {
            System.setProperty("org.slf4j.simpleLogger.logFile", INSTANCE.getLogsLocation().resolve("pdxu.log").toString());
        }
        System.setProperty("org.slf4j.simpleLogger.showThreadName", "false");
        System.setProperty("org.slf4j.simpleLogger.showShortLogName", "true");
        LoggerFactory.getLogger(PdxuInstallation.class).info("Initializing installation at " + installDir.toString());
        Sentry.init();

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

    private PdxuInstallation(Path location, String version, boolean production) {
        this.location = location;
        this.version = version;
        this.production = production;
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
        return location.resolve("achievements");
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
}

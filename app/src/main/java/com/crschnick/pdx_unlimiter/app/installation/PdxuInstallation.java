package com.crschnick.pdx_unlimiter.app.installation;

import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.Optional;

public class PdxuInstallation {

    private static PdxuInstallation INSTANCE;

    public static boolean init() throws Exception {
        // Start from bin directory
        Path p = Path.of(PdxuInstallation.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        Path appV = p.getParent().resolve("version");
        String v;
        Path installDir;
        if (Files.exists(appV)) {
            v = Files.readString(p);
            installDir = p.getParent().getParent();
        } else {
            v = "dev";
            var pa = System.getProperties();
            String dir = Optional.ofNullable(System.getProperty("pdxu.installDir"))
                    .orElseThrow(() -> new NoSuchElementException("Property pdxu.installDir missing for dev build"));
            installDir = Path.of(dir);
        }
        INSTANCE = new PdxuInstallation(installDir, v);

        if (INSTANCE.getNewLauncherLocation().toFile().exists()) {
            FileUtils.deleteDirectory(INSTANCE.getLauncherLocation().toFile());
            FileUtils.moveDirectory(INSTANCE.getNewLauncherLocation().toFile(), INSTANCE.getLauncherLocation().toFile());
        }

        if (INSTANCE.isAlreadyRunning()) {
            return false;
        }

        return true;
    }

    private boolean isAlreadyRunning() {
        return ProcessHandle.allProcesses()
                .map(h -> h.info().command().orElse(""))
                .filter(s -> s.equals(getExecutableLocation().toString()))
                .count() >= 2;
    }

    private Path location;
    private String version;

    private PdxuInstallation(Path location, String version) {
        this.location = location;
        this.version = version;
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
}

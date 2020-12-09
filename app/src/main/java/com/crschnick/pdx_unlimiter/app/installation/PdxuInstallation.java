package com.crschnick.pdx_unlimiter.app.installation;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Properties;

public class PdxuInstallation {

    private static PdxuInstallation INSTANCE;
    private Path dataLocation;
    private String version;
    private boolean production;
    private Path officialAchievementsLocation;
    private boolean developerMode;
    private boolean nativeHookEnabled;

    public PdxuInstallation(Path dataLocation, String version, boolean production,
                            Path officialAchievementsLocation, boolean developerMode,
                            boolean nativeHookEnabled) {
        this.dataLocation = dataLocation;
        this.version = version;
        this.production = production;
        this.officialAchievementsLocation = officialAchievementsLocation;
        this.developerMode = developerMode;
        this.nativeHookEnabled = nativeHookEnabled;
    }

    public static void init() throws Exception {
        Path appPath = Path.of(System.getProperty("java.home"));
        boolean prod = appPath.toFile().getName().equals("app")
                || appPath.toFile().getName().equals("image");
        String v = "unknown";
        Path dataDir;
        Path achievementsLocation = null;
        boolean developerMode = false;
        boolean nativeHook = true;

        Properties props = new Properties();
        if (prod) {
            dataDir = Path.of(System.getProperty("user.home"), "Pdx-Unlimiter");
            v = Files.readString(appPath.resolve("version"));
            props.load(Files.newInputStream(dataDir.resolve("pdxu.properties")));
        } else {
            v = "dev";
            props.load(Files.newInputStream(Path.of("pdxu.properties")));
            dataDir = Optional.ofNullable(props.get("dataDir"))
                    .map(val -> Path.of(val.toString()))
                    .filter(val -> val.isAbsolute() && Files.exists(val))
                    .orElseThrow(() -> new NoSuchElementException("Invalid dataDir for dev build"));

            prod = Optional.ofNullable(props.get("simulateProduction"))
                    .map(val -> Boolean.parseBoolean(val.toString()))
                    .orElse(false);
        }
        achievementsLocation = Optional.ofNullable(props.get("achievementDir"))
                .map(val -> Path.of(val.toString()))
                .filter(val -> val.isAbsolute() && Files.exists(val)).orElse(null);
        developerMode = Optional.ofNullable(props.get("developerMode"))
                .map(val -> Boolean.parseBoolean(val.toString()))
                .orElse(false);
        nativeHook = Optional.ofNullable(props.get("enableJNativeHook"))
                .map(val -> Boolean.parseBoolean(val.toString()))
                .orElse(true);

        INSTANCE = new PdxuInstallation(dataDir, v, prod, achievementsLocation, developerMode, nativeHook);
    }


    public static boolean shouldStart() {
        if (INSTANCE.isProduction() && INSTANCE.isAlreadyRunning()) {
            LoggerFactory.getLogger(PdxuInstallation.class).info("A Pdxu instance is already running.");
            return false;
        }
        return true;
    }

    public static PdxuInstallation getInstance() {
        return INSTANCE;
    }

    private Path getDataLocation() {
        return dataLocation;
    }

    private boolean isAlreadyRunning() {
        return ProcessHandle.allProcesses()
                .map(h -> h.info().command().orElse(""))
                .filter(s -> s.equals(getExecutableLocation().toString()))
                .count() >= 2;
    }

    public boolean isNativeHookEnabled() {
        return nativeHookEnabled;
    }

    public Path getExecutableLocation() {
        Path appPath = Path.of(System.getProperty("java.home"));
        if (SystemUtils.IS_OS_WINDOWS) {
            return appPath.resolve("bin").resolve("java.exe");
        } else if (SystemUtils.IS_OS_LINUX) {
            return appPath.resolve("bin").resolve("java");
        } else {
            return appPath.resolve("bin").resolve("java.exe");
        }
    }

    public Path getLogsLocation() {
        if (SystemUtils.IS_OS_WINDOWS) {
            return dataLocation.resolve("logs");
        } else if (SystemUtils.IS_OS_LINUX) {
            return Path.of("var", "logs", "Pdx-Unlimiter");
        } else {
            return dataLocation.resolve("logs");
        }
    }

    public Path getOfficialAchievementsLocation() {
        return officialAchievementsLocation != null ? officialAchievementsLocation : dataLocation.resolve("achievements");
    }

    public Path getUserAchievementsLocation() {
        return getDataLocation().resolve("user_achievements");
    }

    public Path getSettingsLocation() {
        return getDataLocation().resolve("settings");
    }

    public Path getSavegameLocation() {
        return getDataLocation().resolve("savegames");
    }

    public String getVersion() {
        return version;
    }

    public boolean isProduction() {
        return production;
    }

    public boolean isDeveloperMode() {
        return developerMode;
    }
}

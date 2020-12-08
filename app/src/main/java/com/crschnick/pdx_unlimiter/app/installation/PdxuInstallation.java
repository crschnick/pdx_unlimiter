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
    private Path installLocation;
    private String version;
    private boolean production;
    private Path officialAchievementsLocation;
    private boolean developerMode;
    private boolean nativeHookEnabled;

    public PdxuInstallation(Path installLocation, String version, boolean production,
                            Path officialAchievementsLocation, boolean developerMode,
                            boolean nativeHookEnabled) {
        this.installLocation = installLocation;
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
        Path installDir;
        Path achievementsLocation = null;
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


            boolean simulateProd = Optional.ofNullable(props.get("simulateProduction"))
                    .map(val -> Boolean.parseBoolean(val.toString()))
                    .orElse(false);
            prod = simulateProd;
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

        INSTANCE = new PdxuInstallation(installDir, v, prod, achievementsLocation, developerMode, nativeHook);
    }


    public static boolean shouldStart() {
        if (INSTANCE.isAlreadyRunning()) {
            LoggerFactory.getLogger(PdxuInstallation.class).info("A Pdxu instance is already running.");
            return false;
        }
        return true;
    }

    public static PdxuInstallation getInstance() {
        return INSTANCE;
    }

    private Path getDataLocation() {
        return installLocation;
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
        if (SystemUtils.IS_OS_WINDOWS) {
            return getAppLocation().resolve("bin").resolve("java.exe");
        } else if (SystemUtils.IS_OS_LINUX) {
            return getAppLocation().resolve("bin").resolve("java");
        } else {
            return getAppLocation().resolve("bin").resolve("java.exe");
        }
    }

    public Path getLogsLocation() {
        if (SystemUtils.IS_OS_WINDOWS) {
            return installLocation.resolve("logs");
        } else if (SystemUtils.IS_OS_LINUX) {
            return Path.of("var", "logs", "Pdx-Unlimiter");
        } else {
            return installLocation.resolve("logs");
        }
    }

    public Path getAppLocation() {
        return installLocation.resolve("app");
    }

    public Path getOfficialAchievementsLocation() {
        return officialAchievementsLocation != null ? officialAchievementsLocation : installLocation.resolve("achievements");
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

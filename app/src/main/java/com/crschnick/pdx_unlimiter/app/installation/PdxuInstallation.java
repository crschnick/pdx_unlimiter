package com.crschnick.pdx_unlimiter.app.installation;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

public class PdxuInstallation {

    private static PdxuInstallation INSTANCE;
    private Path dataLocation;
    private String version;
    private boolean production;
    private Path officialAchievementsLocation;
    private boolean developerMode;
    private boolean nativeHookEnabled;
    private boolean image;

    public PdxuInstallation(Path dataLocation, String version, boolean production,
                            Path officialAchievementsLocation, boolean developerMode,
                            boolean nativeHookEnabled, boolean image) {
        this.dataLocation = dataLocation;
        this.version = version;
        this.production = production;
        this.officialAchievementsLocation = officialAchievementsLocation;
        this.developerMode = developerMode;
        this.nativeHookEnabled = nativeHookEnabled;
        this.image = image;
    }

    public static void init() {
        Path appPath = Path.of(System.getProperty("java.home"));
        boolean image = appPath.toFile().getName().equals("app")
                || appPath.toFile().getName().equals("image");
        boolean prod = image;
        String v = "unknown";
        Path dataDir;
        Path achievementsLocation = null;
        boolean developerMode = false;
        boolean nativeHook = true;

        Properties props = new Properties();
        if (prod) {
            dataDir = Path.of(System.getProperty("user.home"), "Pdx-Unlimiter");
            try {
                v = Files.readString(appPath.resolve("version"));
            } catch (IOException e) {
                ErrorHandler.handleException(e);
            }

            Path propsFile = dataDir.resolve("settings").resolve("pdxu.properties");
            if (Files.exists(propsFile)) {
                try {
                    props.load(Files.newInputStream(propsFile));

                } catch (IOException e) {
                    ErrorHandler.handleException(e);
                }
            }
        } else {
            v = "dev";
            try {
                props.load(Files.newInputStream(Path.of("pdxu.properties")));
            } catch (IOException e) {
                ErrorHandler.handleException(e);
            }

            dataDir = Optional.ofNullable(props.get("dataDir"))
                    .map(val -> Path.of(val.toString()))
                    .filter(val -> val.isAbsolute())
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

        INSTANCE = new PdxuInstallation(dataDir, v, prod, achievementsLocation, developerMode, nativeHook, image);
    }


    public static boolean shouldStart() {
        if (INSTANCE.isImage() && INSTANCE.isProduction() && INSTANCE.isAlreadyRunning()) {
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
        var procs = ProcessHandle.allProcesses()
                .map(h -> h.info().command().orElse(""))
                .filter(s -> s.equals(getExecutableLocation().toString()))
                .collect(Collectors.toList());
        procs.forEach(p -> LoggerFactory.getLogger(PdxuInstallation.class)
                .info("Detected running pdxu instance: " + p));
        return procs.size() >= 2;
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

    public Path getDefaultSavegameLocation() {
        return getDataLocation().resolve("savegames");
    }

    public Path getSavegameLocation() {
        return Settings.getInstance().getStorageDirectory().orElse(getDefaultSavegameLocation());
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

    public boolean isImage() {
        return image;
    }
}

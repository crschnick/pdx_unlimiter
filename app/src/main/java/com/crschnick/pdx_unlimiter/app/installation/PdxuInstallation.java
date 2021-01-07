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

    private String rakalyVersion;
    private Path dataLocation;
    private String version;
    private boolean production;
    private Path rakalyDir;
    private boolean developerMode;
    private boolean nativeHookEnabled;
    private boolean image;

    public PdxuInstallation(Path dataLocation, String version, boolean production,
                            Path rakalyDir, String rakalyVersion, boolean developerMode,
                            boolean nativeHookEnabled, boolean image) {
        this.dataLocation = dataLocation;
        this.version = version;
        this.production = production;
        this.developerMode = developerMode;
        this.rakalyVersion = rakalyVersion;
        this.rakalyDir = rakalyDir;
        this.nativeHookEnabled = nativeHookEnabled;
        this.image = image;
    }

    public static void init() {
        Path appPath = Path.of(System.getProperty("java.home"));
        boolean image = Files.exists(appPath.resolve("version"));
        boolean prod = image;
        String v = "unknown";
        Path dataDir = Path.of(System.getProperty("user.home"),
                SystemUtils.IS_OS_WINDOWS ? "Pdx-Unlimiter" : ".pdx-unlimiter");
        boolean developerMode = false;
        boolean nativeHook = true;

        Path appInstallPath;
        if (SystemUtils.IS_OS_WINDOWS) {
            appInstallPath = Path.of(System.getenv("LOCALAPPDATA"))
                    .resolve("Programs").resolve("Pdx-Unlimiter");
        } else {
            appInstallPath = Path.of(System.getProperty("user.home"), ".pdx-unlimiter");
        }
        Path rakalyDir = appInstallPath.resolve("rakaly");

        Properties props = new Properties();
        if (prod) {
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

            var customDir = Optional.ofNullable(props.get("dataDir"))
                    .map(val -> Path.of(val.toString()))
                    .filter(Path::isAbsolute);
            if (customDir.isPresent()) {
                dataDir = customDir.get();
            }

            prod = Optional.ofNullable(props.get("simulateProduction"))
                    .map(val -> Boolean.parseBoolean(val.toString()))
                    .orElse(false);

            var altRakalyDir = Optional.ofNullable(props.get("rakalyDir"))
                    .map(val -> Path.of(val.toString()))
                    .filter(val -> val.isAbsolute() && Files.exists(val));
            if (altRakalyDir.isPresent()) {
                rakalyDir = altRakalyDir.get();
            } else {
                if (!Files.exists(rakalyDir)) {
                    throw new NoSuchElementException("Invalid rakalyDir for dev build. " +
                            "Please clone https://github.com/crschnick/pdxu_rakaly and point " +
                            "the property rakalyDir to repo directory");
                }
            }
        }

        String rakalyVersion;
        try {
            rakalyVersion = Files.readString(rakalyDir.resolve("version"));
        } catch (IOException e) {
            ErrorHandler.handleException(e);
            rakalyVersion = "Unknown";
        }

        developerMode = Optional.ofNullable(props.get("developerMode"))
                .map(val -> Boolean.parseBoolean(val.toString()))
                .orElse(false);
        nativeHook = Optional.ofNullable(props.get("enableJNativeHook"))
                .map(val -> Boolean.parseBoolean(val.toString()))
                .orElse(true);

        INSTANCE = new PdxuInstallation(dataDir, v, prod, rakalyDir, rakalyVersion, developerMode, nativeHook, image);
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

    public Path getImportQueueLocation() {
        return dataLocation.resolve("import");
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
        return dataLocation.resolve("logs");
    }

    public Path getRakalyExecutable() {
        Path dir = rakalyDir;
        if (SystemUtils.IS_OS_WINDOWS) {
            return dir.resolve("bin").resolve("rakaly_windows.exe");
        } else if (SystemUtils.IS_OS_LINUX) {
            return dir.resolve("bin").resolve("rakaly_linux");
        } else {
            return dir.resolve("bin").resolve("rakaly_mac");
        }
    }

    public String getRakalyVersion() {
        return rakalyVersion;
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

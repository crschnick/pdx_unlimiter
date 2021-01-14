package com.crschnick.pdx_unlimiter.app.installation;

import com.crschnick.pdx_unlimiter.app.gui.GuiErrorReporter;
import com.crschnick.pdx_unlimiter.app.util.InstallLocationHelper;
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

    private Path dataDir;
    private String version;
    private boolean production;
    private Path rakalyDir;
    private boolean developerMode;
    private boolean nativeHookEnabled;
    private boolean image;

    public static void init() {
        INSTANCE = new PdxuInstallation();

        Path appPath = Path.of(System.getProperty("java.home"));
        INSTANCE.image = Files.exists(appPath.resolve("version"));
        INSTANCE.production = INSTANCE.image;
        INSTANCE.version = "unknown";

        // Legacy support
        var legacyDataDir = Path.of(System.getProperty("user.home"),
                SystemUtils.IS_OS_WINDOWS ? "Pdx-Unlimiter" : ".pdx-unlimiter");
        if (Files.exists(legacyDataDir)) {
            INSTANCE.dataDir = legacyDataDir;
        } else {
            INSTANCE.dataDir = InstallLocationHelper.getUserDocumentsPath().resolve("Pdx-Unlimiter");
        }

        Path appInstallPath;
        if (SystemUtils.IS_OS_WINDOWS) {
            appInstallPath = Path.of(System.getenv("LOCALAPPDATA"))
                    .resolve("Programs").resolve("Pdx-Unlimiter");
        } else {
            appInstallPath = INSTANCE.dataDir;
        }
        INSTANCE.rakalyDir = appInstallPath.resolve("rakaly");

        Properties props = new Properties();
        if (INSTANCE.production) {
            try {
                INSTANCE.version = Files.readString(appPath.resolve("version"));
            } catch (IOException e) {
                ErrorHandler.handleException(e);
            }

            Path propsFile = INSTANCE.dataDir.resolve("settings").resolve("pdxu.properties");
            if (Files.exists(propsFile)) {
                try {
                    props.load(Files.newInputStream(propsFile));
                } catch (IOException e) {
                    ErrorHandler.handleException(e);
                }
            }

            if (!Files.exists(INSTANCE.rakalyDir)) {
                GuiErrorReporter.showErrorMessage(
                        "The rakaly installation seems to be invalid.",
                        "This can be caused by a failed update." +
                                "Please try restarting the Pdx-Unlimiter", true, false);
            }
        } else {
            INSTANCE.version = "dev";
            try {
                props.load(Files.newInputStream(Path.of("pdxu.properties")));
            } catch (IOException e) {
                ErrorHandler.handleException(e);
            }

            var customDir = Optional.ofNullable(props.get("dataDir"))
                    .map(val -> Path.of(val.toString()))
                    .filter(Path::isAbsolute);
            customDir.ifPresent(path -> INSTANCE.dataDir = path);

            INSTANCE.production = Optional.ofNullable(props.get("simulateProduction"))
                    .map(val -> Boolean.parseBoolean(val.toString()))
                    .orElse(false);

            Optional.ofNullable(props.get("rakalyDir"))
                    .map(val -> Path.of(val.toString()))
                    .filter(val -> val.isAbsolute() && Files.exists(val))
                    .ifPresent(path -> INSTANCE.rakalyDir = path);
        }

        INSTANCE.developerMode = Optional.ofNullable(props.get("developerMode"))
                .map(val -> Boolean.parseBoolean(val.toString()))
                .orElse(false);
        INSTANCE.nativeHookEnabled = Optional.ofNullable(props.get("enableJNativeHook"))
                .map(val -> Boolean.parseBoolean(val.toString()))
                .orElse(true);
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

    private Path getDataDir() {
        return dataDir;
    }

    public Path getImportQueueLocation() {
        return dataDir.resolve("import");
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
        return dataDir.resolve("logs");
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

    public Path getSettingsLocation() {
        return getDataDir().resolve("settings");
    }

    public Path getDefaultSavegamesLocation() {
        return getDataDir().resolve("savegames");
    }

    public Path getSavegamesLocation() {
        return Settings.getInstance().getStorageDirectory().orElse(getDefaultSavegamesLocation());
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

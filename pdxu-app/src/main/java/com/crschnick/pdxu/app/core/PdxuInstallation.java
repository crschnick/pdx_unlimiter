package com.crschnick.pdxu.app.core;

import com.crschnick.pdxu.app.util.OsHelper;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

public class PdxuInstallation {

    private static PdxuInstallation INSTANCE;

    private Path dataDir;
    private String version;
    private boolean production;
    private boolean developerMode;
    private boolean nativeHookEnabled;
    private boolean image;
    private Path eu4SeDir;
    private Path languageDir;
    private Path resourceDir;
    private boolean preRelease;
    private String latestVersion;

    public static void init() {
        var i = new PdxuInstallation();

        Path appPath = Path.of(System.getProperty("java.home"));
        i.image = Files.exists(appPath.resolve("version"));
        i.production = i.image;
        i.version = "unknown";

        if (i.image) {
            i.languageDir = appPath.resolve("lang");
            i.resourceDir = appPath.resolve("resources");
        } else {
            i.languageDir = Path.of("lang");
            i.resourceDir = Path.of("resources");
        }

        // Legacy support
        var legacyDataDir = Path.of(System.getProperty("user.home"),
                SystemUtils.IS_OS_WINDOWS ? "Pdx-Unlimiter" : ".pdx-unlimiter");
        if (Files.exists(legacyDataDir)) {
            i.dataDir = legacyDataDir;
        } else {
            i.dataDir = OsHelper.getUserDocumentsPath().resolve("Pdx-Unlimiter");
        }

        Path appInstallPath;
        if (SystemUtils.IS_OS_WINDOWS) {
            appInstallPath = Path.of(System.getenv("LOCALAPPDATA"))
                    .resolve("Programs").resolve("Pdx-Unlimiter");
        } else {
            appInstallPath = i.dataDir;
        }

        i.eu4SeDir = appInstallPath.resolveSibling("Eu4SaveEditor");
        if (!Files.exists(i.eu4SeDir)) {
            i.eu4SeDir = null;
        }

        var latestFile = i.dataDir.resolve("settings").resolve("latest");
        if (Files.exists(latestFile)) {
            try {
                i.latestVersion = Files.readString(i.dataDir.resolve("settings").resolve("latest"));
            } catch (IOException e) {
                ErrorHandler.handleException(e);
            }
        }

        Properties props = new Properties();
        if (i.production) {
            try {
                i.version = Files.readString(appPath.resolve("version"));
            } catch (IOException e) {
                ErrorHandler.handleException(e);
            }

            Path propsFile = i.dataDir.resolve("settings").resolve("pdxu.properties");
            if (Files.exists(propsFile)) {
                try {
                    props.load(Files.newInputStream(propsFile));
                } catch (IOException e) {
                    ErrorHandler.handleException(e);
                }
            }
        } else {
            i.version = "dev";
            try {
                props.load(Files.newInputStream(Path.of("pdxu.properties")));
            } catch (IOException e) {
                ErrorHandler.handleException(e);
            }

            i.languageDir = Path.of("lang");

            var customDir = Optional.ofNullable(props.get("dataDir"))
                    .map(val -> Path.of(val.toString()))
                    .filter(Path::isAbsolute);
            customDir.ifPresent(path -> i.dataDir = path);

            i.production = Optional.ofNullable(props.get("simulateProduction"))
                    .map(val -> Boolean.parseBoolean(val.toString()))
                    .orElse(false);
        }

        i.developerMode = Optional.ofNullable(props.get("developerMode"))
                .map(val -> Boolean.parseBoolean(val.toString()))
                .orElse(false);
        i.nativeHookEnabled = Optional.ofNullable(props.get("enableJNativeHook"))
                .map(val -> Boolean.parseBoolean(val.toString()))
                .orElse(true);

        i.preRelease = i.version.contains("pre");

        INSTANCE = i;
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

    public Path getLanguageLocation() {
        return languageDir;
    }

    public Path getLogsLocation() {
        return dataDir.resolve("logs");
    }

    public Path getRakalyExecutable() {
        Path dir = getResourceDir();
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

    public boolean isEu4SaveEditorInstalled() {
        return getEu4SaveEditorLocation() != null;
    }

    public Path getEu4SaveEditorLocation() {
        return eu4SeDir;
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

    public Path getResourceDir() {
        return resourceDir;
    }

    public boolean isPreRelease() {
        return preRelease;
    }

    public String getLatestVersion() {
        return latestVersion;
    }
}

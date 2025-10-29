package com.crschnick.pdxu.app.core;

import com.crschnick.pdxu.app.util.OsHelper;
import com.crschnick.pdxu.app.util.SupportedOs;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;

public class PdxuInstallation {

    private static PdxuInstallation INSTANCE;

    private Path dataDir;
    private String version;
    private boolean production;
    private boolean developerMode;
    private boolean nativeHookEnabled;
    private boolean image;
    private Path languageDir;
    private Path resourceDir;
    private boolean preRelease;
    private String latestVersion;
    private String logLevel = "debug";
    private boolean standalone;

    private static Path getAppPath() {
        Path path = Path.of(System.getProperty("java.home"));
        switch (SupportedOs.get()) {
            case WINDOWS -> {
                return path.getParent();
            }
            case LINUX -> {
                return path.getParent().getParent();
            }
            case MAC -> {
                return path.getParent().getParent().getParent().getParent();
            }
        }

        throw new AssertionError();
    }

    public static void checkCorrectExtraction() {
        boolean image = PdxuInstallation.class.getProtectionDomain().getCodeSource().getLocation().getProtocol().equals("jrt");
        boolean invalid = image && (!Files.exists(getLanguageDirectory())
                || !Files.exists(getResourcesDirectory()));
        if (invalid) {
            ErrorHandler.handleTerminalException(new IOException("Required files not found. " +
                                                                         "If you use the standalone distribution, please check whether you " +
                                                                         "extracted the archive correctly."));
        }
    }

    public static void checkDataDirectoryPermissions() {
        Path dataDir;
        // Legacy support
        var legacyDataDir = Path.of(
                System.getProperty("user.home"),
                SystemUtils.IS_OS_WINDOWS ? "Pdx-Unlimiter" : ".pdx-unlimiter"
        );
        if (Files.exists(legacyDataDir)) {
            dataDir = legacyDataDir;
        } else {
            dataDir = OsHelper.getUserDocumentsPath().resolve("Pdx-Unlimiter");
        }

        try {
            Files.createDirectories(dataDir);
            var testDirectory = dataDir.resolve("permissions_check");
            Files.createDirectories(testDirectory);
            Files.delete(testDirectory);
        } catch (IOException e) {
            ErrorHandler.handleTerminalException(new IOException(
                    "Unable to access directory " + dataDir.getParent() + ". Please make sure that you have the appropriate permissions and no Antivirus program is blocking the access (Windows Defender often does that). " +
                            "In case you use cloud storage, verify that your cloud storage is working and you are logged in."));
        }
    }

    private static Path getVersionFile() {
        Path appPath = getAppPath();
        return SystemUtils.IS_OS_MAC ? appPath.resolve("Contents").resolve("Resources").resolve("version") : appPath.resolve("version");
    }

    private static Path getLanguageDirectory() {
        Path appPath = getAppPath();
        boolean image = PdxuInstallation.class.getProtectionDomain().getCodeSource().getLocation().getProtocol().equals("jrt");
        if (image) {
            return SystemUtils.IS_OS_MAC ? appPath.resolve("Contents").resolve("Resources").resolve("lang") : appPath.resolve(
                    "lang");
        } else {
            return Path.of("lang");
        }
    }

    private static Path getResourcesDirectory() {
        Path appPath = getAppPath();
        boolean image = PdxuInstallation.class.getProtectionDomain().getCodeSource().getLocation().getProtocol().equals("jrt");
        if (image) {
            return SystemUtils.IS_OS_MAC ? appPath.resolve("Contents").resolve("Resources").resolve("resources") : appPath.resolve(
                    "resources");
        } else {
            return Path.of("resources");
        }
    }

    private  static boolean checkImage() {
        return PdxuInstallation.class
                .getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .getProtocol()
                .equals("jrt");
    }


    public static void init() {
        var i = new PdxuInstallation();

        Path appPath = getAppPath();
        i.image = checkImage();
        i.production = i.image;
        i.version = "unknown";
        i.languageDir = getLanguageDirectory();
        i.resourceDir = getResourcesDirectory();

        // Legacy support
        var legacyDataDir = Path.of(
                System.getProperty("user.home"),
                SystemUtils.IS_OS_WINDOWS ? "Pdx-Unlimiter" : ".pdx-unlimiter"
        );
        if (Files.exists(legacyDataDir)) {
            i.dataDir = legacyDataDir;
        } else {
            i.dataDir = OsHelper.getUserDocumentsPath().resolve("Pdx-Unlimiter");
        }

        Path appInstallPath = null;
        switch (SupportedOs.get()) {
            case WINDOWS -> {
                appInstallPath = Path.of(System.getenv("LOCALAPPDATA"))
                        .resolve("Programs").resolve("Pdx-Unlimiter");
            }
            case LINUX, MAC -> {
                appInstallPath = i.dataDir;
            }
        }

        Path defaultAppInstallPath = appInstallPath.resolve("app");
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
            i.standalone = !appPath.equals(defaultAppInstallPath);

            try {
                i.version = Files.readString(getVersionFile());
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
            if (!Files.exists(Path.of("pdxu.properties"))) {
                System.err.println("You are running pdxu in a dev environment without proper setup. " +
                                           "See https://github.com/crschnick/pdx_unlimiter#development for details.");
                System.exit(1);
            }

            try {
                props.load(Files.newInputStream(Path.of("pdxu.properties")));
            } catch (IOException e) {
                ErrorHandler.handleException(e);
            }

            var customDir = Optional.ofNullable(props.get("dataDir"))
                    .map(val -> Path.of(val.toString()))
                    .filter(Path::isAbsolute);
            customDir.ifPresent(path -> i.dataDir = path);

            i.standalone = Optional.ofNullable(props.get("simulateStandalone"))
                    .map(val -> Boolean.parseBoolean(val.toString()))
                    .orElse(!appPath.equals(defaultAppInstallPath));

            i.production = Optional.ofNullable(props.get("simulateProduction"))
                    .map(val -> Boolean.parseBoolean(val.toString()))
                    .orElse(false);
        }

        i.logLevel = Optional.ofNullable(props.get("logLevel"))
                .map(Object::toString)
                .orElse("debug");

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

    public Path getDataDir() {
        return dataDir;
    }

    public Path getImportQueueLocation() {
        return dataDir.resolve("import");
    }

    private boolean isAlreadyRunning() {
        var procs = ProcessHandle.allProcesses()
                .filter(h -> h.info().command().orElse("").equals(getExecutableLocation().toString())).toList();
        procs.forEach(p -> LoggerFactory.getLogger(PdxuInstallation.class)
                .info("Detected running pdxu instance: " + p.info().command().orElse("") + " (PID " + p.pid() + ")"));
        return procs.size() >= 3;
    }

    public boolean isNativeHookEnabled() {
        return nativeHookEnabled;
    }

    public Path getJavaExecutableLocation() {
        Path appPath = getAppPath();
        switch (SupportedOs.get()) {
            case WINDOWS -> {
                return appPath.resolve("runtime").resolve("bin").resolve("java.exe");
            }
            case LINUX -> {
                return appPath.resolve("lib").resolve("runtime").resolve("bin").resolve("java");
            }
            case MAC -> {
                return appPath.resolve("Contents")
                        .resolve("runtime")
                        .resolve("Contents")
                        .resolve("Home")
                        .resolve("bin");
            }
            default -> {
                return null;
            }
        }
    }

    public Path getExecutableLocation() {
        Path appPath = getAppPath();
        switch (SupportedOs.get()) {
            case WINDOWS -> {
                return appPath.resolve("Pdx-Unlimiter.exe");
            }
            case LINUX -> {
                return appPath.resolve("bin").resolve("Pdx-Unlimiter");
            }
            case MAC -> {
                return appPath.resolve("Contents").resolve("MacOS").resolve("Pdx-Unlimiter");
            }
            default -> {
                return null;
            }
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
        switch (SupportedOs.get()) {
            case WINDOWS -> {
                return dir.resolve("bin").resolve("rakaly_windows.exe");
            }
            case LINUX -> {
                return dir.resolve("bin").resolve("rakaly_linux");
            }
            case MAC -> {
                return getAppPath().resolve("Contents").resolve("MacOS").resolve("rakaly_mac");
            }
            default -> {
                return null;
            }
        }
    }

    public Path getSettingsLocation() {
        return getDataDir().resolve("settings");
    }

    public Path getDefaultSavegamesLocation() {
        return getDataDir().resolve("savegames");
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

    public String getLogLevel() {
        return logLevel;
    }

    public boolean isStandalone() {
        return standalone;
    }
}

package com.crschnick.pdx_unlimiter.app.core;

import com.crschnick.pdx_unlimiter.app.util.OsHelper;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.stream.Collectors;

public class PdxuInstallation {

    private static PdxuInstallation INSTANCE;

    private Path dataDir;
    private String version;
    private boolean production;
    private boolean image;
    private Path rakalyDir;
    private Path eu4SeDir;
    private Path languageDir;
    private Path resourceDir;
    private String latestVersion;
    private PdxuProperties properties;
    private UUID userId;

    private PdxuInstallation() {
        initEnvironment();
        initDefaultDataDir();
        initVersion();

        properties = new PdxuProperties(production, getSettingsLocation());
        production = properties.isSimulateProduction() || production;

        initLatestVersion();
        initResourceDirs();
        initThirdPartyDirs();
        initUserId();
    }

    private void initEnvironment() {
        Path appPath = Path.of(System.getProperty("java.home"));
        image = Files.exists(appPath.resolve("version"));
        production = image;
    }

    private void initThirdPartyDirs() {
        Path appInstallPath;
        if (SystemUtils.IS_OS_WINDOWS) {
            appInstallPath = Path.of(System.getenv("LOCALAPPDATA"))
                    .resolve("Programs").resolve("Pdx-Unlimiter");
        } else {
            appInstallPath = dataDir;
        }

        rakalyDir = properties.getCustomRakalyDir().orElse(appInstallPath.resolve("rakaly"));

        eu4SeDir = appInstallPath.resolveSibling("Eu4SaveEditor");
        if (!Files.exists(eu4SeDir)) {
            eu4SeDir = null;
        }
    }

    private void initDefaultDataDir() {
        // Legacy support
        var legacyDataDir = Path.of(System.getProperty("user.home"),
                SystemUtils.IS_OS_WINDOWS ? "Pdx-Unlimiter" : ".pdx-unlimiter");
        if (Files.exists(legacyDataDir)) {
            dataDir = legacyDataDir;
        } else {
            dataDir = OsHelper.getUserDocumentsPath().resolve("Pdx-Unlimiter");
        }
    }

    private void initUserId() {
        var idFile = getSettingsLocation().resolve("user");
        if (Files.exists(idFile)) {
            try {
                userId = UUID.fromString(Files.readString(idFile));
            } catch (Exception e) {
                ErrorHandler.handleException(e);
            }
        } else {
            userId = UUID.randomUUID();
            try {
                Files.writeString(idFile, userId.toString());
            } catch (Exception e) {
                ErrorHandler.handleException(e);
            }
        }
    }

    private void initLatestVersion() {
        var latestFile = getSettingsLocation().resolve("latest");
        if (Files.exists(latestFile)) {
            try {
                latestVersion = Files.readString(latestFile);
            } catch (IOException e) {
                ErrorHandler.handleException(e);
            }
        }
    }

    private void initVersion() {
        if (production) {
            try {
                Path appPath = Path.of(System.getProperty("java.home"));
                version = Files.readString(appPath.resolve("version"));
            } catch (IOException e) {
                ErrorHandler.handleException(e);
            }
        } else {
            version = "dev";
        }
    }

    private void initResourceDirs() {
        if (image) {
            Path appPath = Path.of(System.getProperty("java.home"));
            languageDir = appPath.resolve("lang");
            resourceDir = appPath.resolve("resources");
        } else {
            languageDir = Path.of("app", "lang");
            resourceDir = Path.of("app", "resources");
        }
    }

    public static void init() {
        INSTANCE = new PdxuInstallation();
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

    public Path getLazyImportStorageLocation() {
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
        return properties.isNativeHookEnabled();
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
        return properties.isDeveloperMode();
    }

    public boolean isImage() {
        return image;
    }

    public Path getResourceDir() {
        return resourceDir;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public UUID getUserId() {
        return userId;
    }
}

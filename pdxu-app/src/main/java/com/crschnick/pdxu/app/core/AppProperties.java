package com.crschnick.pdxu.app.core;

import com.crschnick.pdxu.app.core.check.AppDirectoryPermissionsCheck;
import com.crschnick.pdxu.app.core.mode.AppOperationModeSelection;
import com.crschnick.pdxu.app.issue.ErrorEventFactory;
import com.crschnick.pdxu.app.issue.TrackEvent;

import com.crschnick.pdxu.app.util.FileSystemHelper;
import lombok.Value;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Value
public class AppProperties {

    private static AppProperties INSTANCE;

    String version;
    String build;
    UUID buildUuid;
    String sentryUrl;
    String arch;
    boolean image;
    boolean useVirtualThreads;
    Path dataDir;
    Path defaultDataDir;
    boolean showcase;
    AppVersion canonicalVersion;
    UUID uuid;
    boolean initialLaunch;
    UUID sessionId;
    boolean newBuildSession;
    boolean aotTrainMode;
    boolean debugPlatformThreadAccess;
    AppArguments arguments;
    AppOperationModeSelection explicitMode;
    boolean logToSysOut;
    boolean logToFile;
    String logLevel;
    boolean nativeHookEnabled;

    public AppProperties(String[] args) {
        var appDir = Path.of(System.getProperty("user.dir")).resolve("app");
        Path propsFile = appDir.resolve("dev.properties");
        if (Files.exists(propsFile)) {
            try {
                Properties props = new Properties();
                props.load(Files.newInputStream(propsFile));
                props.forEach((key, value) -> {
                    // Don't overwrite existing properties
                    if (System.getProperty(key.toString()) == null) {
                        System.setProperty(key.toString(), value.toString());
                    }
                });
            } catch (IOException e) {
                ErrorEventFactory.fromThrowable(e).handle();
            }
        }
        var referenceDir = Files.exists(appDir) ? appDir : Path.of(System.getProperty("user.dir"));

        image = AppProperties.class
                .getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .getProtocol()
                .equals("jrt");
        arguments = AppArguments.init(args);
        version = Optional.ofNullable(System.getProperty(AppNames.propertyName("version")))
                .orElse("dev");
        build = Optional.ofNullable(System.getProperty(AppNames.propertyName("build")))
                .orElse("unknown");
        buildUuid = Optional.ofNullable(System.getProperty(AppNames.propertyName("buildId")))
                .map(UUID::fromString)
                .orElse(UUID.randomUUID());
        sentryUrl = System.getProperty(AppNames.propertyName("sentryUrl"));
        arch = System.getProperty("os.arch").equals("amd64") ? "x86_64" : "arm64";
        useVirtualThreads = Optional.ofNullable(System.getProperty(AppNames.propertyName("useVirtualThreads")))
                .map(Boolean::parseBoolean)
                .orElse(true);
        debugPlatformThreadAccess = Optional.ofNullable(
                        System.getProperty(AppNames.propertyName("debugPlatformThreadAccess")))
                .map(Boolean::parseBoolean)
                .orElse(false);
        showcase = Optional.ofNullable(System.getProperty(AppNames.propertyName("showcase")))
                .map(Boolean::parseBoolean)
                .orElse(false);
        canonicalVersion = AppVersion.parse(version).orElse(null);
        logToSysOut = Optional.ofNullable(System.getProperty(AppNames.propertyName("writeSysOut")))
                .map(Boolean::parseBoolean)
                .orElse(false);
        logToFile = Optional.ofNullable(System.getProperty(AppNames.propertyName("writeLogs")))
                .map(Boolean::parseBoolean)
                .orElse(true);
        logLevel = Optional.ofNullable(System.getProperty(AppNames.propertyName("logLevel")))
                .filter(AppLogs.LOG_LEVELS::contains)
                .orElse("info");
        nativeHookEnabled = Optional.ofNullable(System.getProperty(AppNames.propertyName("enableJNativeHook")))
                .map(Boolean::parseBoolean)
                .orElse(true);

        defaultDataDir = FileSystemHelper.getUserDocumentsPath().resolve(AppNames.ofCurrent().getName());
        dataDir = Optional.ofNullable(System.getProperty(AppNames.propertyName("dataDir")))
                .map(s -> {
                    var p = Path.of(s);
                    if (!p.isAbsolute()) {
                        p = referenceDir.resolve(p);
                    }
                    return p;
                })
                .orElse(defaultDataDir);

        // We require the user dir from here
        AppDirectoryPermissionsCheck.checkDirectory(dataDir);
        AppCache.setBasePath(dataDir.resolve("cache"));
        UUID id = AppCache.getNonNull("uuid", UUID.class, () -> null);
        if (id == null) {
            uuid = UUID.randomUUID();
            AppCache.update("uuid", uuid);
        } else {
            uuid = id;
        }
        initialLaunch = AppCache.getNonNull("lastBuildId", String.class, () -> null) == null;
        sessionId = UUID.randomUUID();
        var cachedBuildId = AppCache.getNonNull("lastBuildId", String.class, () -> null);
        newBuildSession = !buildUuid.toString().equals(cachedBuildId);
        AppCache.update("lastBuildId", buildUuid);
        aotTrainMode = Optional.ofNullable(System.getProperty(AppNames.propertyName("aotTrainMode")))
                .map(Boolean::parseBoolean)
                .orElse(false);
        explicitMode = AppOperationModeSelection.getIfPresent(System.getProperty(AppNames.propertyName("mode")))
                .orElse(null);
    }

    public static void init() {
        init(new String[0]);
    }

    public static void init(String[] args) {
        if (INSTANCE != null) {
            return;
        }

        INSTANCE = new AppProperties(args);
    }

    public static AppProperties get() {
        return INSTANCE;
    }

    public void logArguments() {
        TrackEvent.withInfo("Loaded properties")
                .tag("version", version)
                .tag("build", build)
                .tag("dataDir", dataDir)
                .handle();

        TrackEvent.withInfo("Received arguments")
                .tag("raw", arguments.getRawArgs())
                .tag("resolved", arguments.getResolvedArgs())
                .tag("resolvedCommand", arguments.getOpenArgs())
                .handle();

        for (var e : System.getProperties().entrySet()) {
            if (e.getKey().toString().contains(AppNames.ofCurrent().getGroupName())) {
                TrackEvent.debug("Detected app property " + e.getKey() + "=" + e.getValue());
            }
        }
    }

    public boolean isDevelopmentEnvironment() {
        return !isImage();
    }

    public Optional<AppVersion> getCanonicalVersion() {
        return Optional.ofNullable(canonicalVersion);
    }
}

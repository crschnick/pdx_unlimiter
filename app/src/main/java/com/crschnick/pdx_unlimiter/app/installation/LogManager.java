package com.crschnick.pdx_unlimiter.app.installation;

import org.apache.commons.io.FileUtils;
import org.jnativehook.GlobalScreen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.logging.Level;

public class LogManager {

    private static LogManager INSTANCE;

    private Path logFile;
    private boolean debugInstallations;
    private boolean debugAchievements;

    private LogManager(Path logFile, boolean debugInstallations, boolean debugAchievements) {
        this.logFile = logFile;
        this.debugInstallations = debugInstallations;
        this.debugAchievements = debugAchievements;
    }

    private void setLogLevels(boolean debug) {
        if (debug) {
            System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
        }

        if (!debugAchievements) {
            System.setProperty("org.slf4j.simpleLogger.log.com.crschnick.pdx_unlimiter.app.achievement", "info");
        }

        if (!debugInstallations) {
            System.setProperty("org.slf4j.simpleLogger.log.com.crschnick.pdx_unlimiter.app.installation", "info");
        }

        System.setProperty("org.slf4j.simpleLogger.log.com.jayway.jsonpath.internal.path.CompiledPath", "warn");
        System.setProperty("org.slf4j.simpleLogger.log.com.jayway.jsonpath.internal.path.PredicateContextImpl", "warn");

        if (PdxuInstallation.getInstance().isNativeHookEnabled()) {
            java.util.logging.Logger logger = java.util.logging.Logger.getLogger(GlobalScreen.class.getPackage().getName());
            logger.setLevel(Level.OFF);
            logger.setUseParentHandlers(false);
            GlobalScreen.getAutoRepeatDelay();
        }
    }

    public static void init() throws IOException {
        PdxuInstallation i = PdxuInstallation.getInstance();

        FileUtils.forceMkdir(i.getLogsLocation().toFile());

        Path logFile = null;
        if (i.isProduction()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")
                    .withZone(ZoneId.systemDefault());
            logFile = i.getLogsLocation().resolve("pdxu_" + formatter.format(Instant.now()) + ".log");
            System.setProperty("org.slf4j.simpleLogger.logFile", logFile.toString());
        }

        System.setProperty("org.slf4j.simpleLogger.showThreadName", "false");
        System.setProperty("org.slf4j.simpleLogger.showShortLogName", "true");

        INSTANCE = new LogManager(logFile, true, false);
        INSTANCE.setLogLevels(i.isDeveloperMode());

        Logger l = LoggerFactory.getLogger(LogManager.class);
        l.info("Initializing LogManager");
        if (logFile != null) {
            l.info("Writing to log file " + logFile.toString());
        }
    }

    public static LogManager getInstance() {
        return INSTANCE;
    }

    public Optional<Path> getLogFile() {
        return Optional.ofNullable(logFile);
    }
}

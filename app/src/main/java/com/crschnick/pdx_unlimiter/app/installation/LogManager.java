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

    public LogManager(Optional<Path> logFile) {
        this.logFile = logFile;
    }

    private static void setLogLevels(boolean debug) {
        if (debug) {
            System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
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

        Optional<Path> logFile = Optional.empty();
        if (i.isProduction()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")
                    .withZone(ZoneId.systemDefault());
            logFile = Optional.of(i.getLogsLocation().resolve("pdxu_" + formatter.format(Instant.now()) + ".log"));
            System.setProperty("org.slf4j.simpleLogger.logFile", logFile.get().toString());
        }
        setLogLevels(i.isDeveloperMode());

        System.setProperty("org.slf4j.simpleLogger.showThreadName", "false");
        System.setProperty("org.slf4j.simpleLogger.showShortLogName", "true");


        Logger l = LoggerFactory.getLogger(LogManager.class);
        l.info("Initializing LogManager");

        INSTANCE = new LogManager(logFile);
    }

    private Optional<Path> logFile;

    public Optional<Path> getLogFile() {
        return logFile;
    }

    public static LogManager getInstance() {
        return INSTANCE;
    }
}

package com.crschnick.pdxu.app.core;

import org.apache.commons.io.FileUtils;
import org.jnativehook.GlobalScreen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.logging.Level;

public class LogManager {

    private static LogManager INSTANCE;

    private final Path logFile;
    private final boolean debugInstallations;

    private LogManager(Path logFile, boolean debugInstallations) {
        this.logFile = logFile;
        this.debugInstallations = debugInstallations;
    }

    public static void init() {
        PdxuInstallation i = PdxuInstallation.getInstance();


        Path logFile = null;
        if (i.isProduction()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")
                    .withZone(ZoneId.systemDefault());
            try {
                FileUtils.forceMkdir(i.getLogsLocation().toFile());
                logFile = i.getLogsLocation().resolve("pdxu_" + formatter.format(Instant.now()) + ".log");
                System.setProperty("org.slf4j.simpleLogger.logFile", logFile.toString());
            } catch (IOException e) {
                ErrorHandler.handleException(e);
            }
        }

        System.setProperty("org.slf4j.simpleLogger.showDateTime", "true");
        System.setProperty("org.slf4j.simpleLogger.dateTimeFormat", "HH:mm:ss");
        System.setProperty("org.slf4j.simpleLogger.showThreadName", "false");
        System.setProperty("org.slf4j.simpleLogger.showShortLogName", "true");

        INSTANCE = new LogManager(logFile, true);
        INSTANCE.setLogLevels();

        Logger l = LoggerFactory.getLogger(LogManager.class);
        l.info("Initializing LogManager");
        if (logFile != null) {
            l.info("Writing to log file " + logFile.toString());

            System.setOut(new PrintStream(new OutputStream() {
                private final ByteArrayOutputStream baos = new ByteArrayOutputStream(1000);

                @Override
                public void write(int b) {
                    if (b == '\n') {
                        String line = baos.toString();
                        LoggerFactory.getLogger("stdout").info(line.strip());
                        baos.reset();
                    } else {
                        baos.write(b);
                    }
                }
            }));
            System.setErr(new PrintStream(new OutputStream() {
                private final ByteArrayOutputStream baos = new ByteArrayOutputStream(1000);

                @Override
                public void write(int b) {
                    if (b == '\n') {
                        String line = baos.toString();
                        LoggerFactory.getLogger("stderr").error(line.strip());
                        baos.reset();
                    } else {
                        baos.write(b);
                    }
                }
            }));
        }
    }

    public static LogManager getInstance() {
        return INSTANCE;
    }

    private void setLogLevels() {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", PdxuInstallation.getInstance().getLogLevel());

        // Debug output for platform
        // System.setProperty("prism.verbose", "true");

        if (!debugInstallations) {
            System.setProperty("org.slf4j.simpleLogger.log.com.crschnick.pdx_unlimiter.app.installation", "info");
        }

        if (PdxuInstallation.getInstance().isNativeHookEnabled()) {
            java.util.logging.Logger logger = java.util.logging.Logger.getLogger(GlobalScreen.class.getPackage().getName());
            logger.setLevel(Level.OFF);
            logger.setUseParentHandlers(false);
            GlobalScreen.isNativeHookRegistered();
        }
    }

    public Optional<Path> getLogFile() {
        return Optional.ofNullable(logFile);
    }
}

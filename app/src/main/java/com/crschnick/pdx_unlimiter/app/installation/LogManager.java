package com.crschnick.pdx_unlimiter.app.installation;

import io.sentry.Sentry;
import io.sentry.SentryOptions;
import org.apache.commons.io.FileUtils;
import org.jnativehook.GlobalScreen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.logging.Level;

public class LogManager {

    private static LogManager INSTANCE;

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

        if (i.isProduction()) {
            System.setProperty("org.slf4j.simpleLogger.logFile", i.getLogsLocation().resolve("pdxu.log").toString());
        }
        setLogLevels(i.isDeveloperMode());

        System.setProperty("org.slf4j.simpleLogger.showThreadName", "false");
        System.setProperty("org.slf4j.simpleLogger.showShortLogName", "true");


        Logger l = LoggerFactory.getLogger(LogManager.class);
        l.info("Initializing LogManager");
        l.info("Working directory: " + System.getProperty("user.dir"));

        Sentry.init();

        INSTANCE = new LogManager();
    }
}

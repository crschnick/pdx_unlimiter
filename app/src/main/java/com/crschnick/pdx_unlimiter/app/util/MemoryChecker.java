package com.crschnick.pdx_unlimiter.app.util;

import com.crschnick.pdx_unlimiter.app.gui.DialogHelper;
import com.crschnick.pdx_unlimiter.app.installation.ComponentManager;
import javafx.application.Platform;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class MemoryChecker {
    private static final long MIN_FREE_MEMORY = 600000000;

    public static boolean checkForEnoughMemory() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long allocatedMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();

        long totalFreeMemory = (freeMemory + (maxMemory - allocatedMemory));
        LoggerFactory.getLogger(MemoryChecker.class).debug("Checking for free memory: " + totalFreeMemory / 1024 + "kB");
        if (totalFreeMemory < MIN_FREE_MEMORY) {
            AtomicBoolean shouldExit = new AtomicBoolean(false);
            CountDownLatch latch = new CountDownLatch(1);
            Runnable r = () -> {
                shouldExit.set(DialogHelper.showLowMemoryDialog());
                latch.countDown();
            };
            if (Platform.isFxApplicationThread()) {
                r.run();
            } else {
                Platform.runLater(r);
            }

            try {
                latch.await();
            } catch (InterruptedException ignored) {
            }

            if (shouldExit.get()) {
                ComponentManager.finalTeardown();
                return false;
            }
        } else {
            LoggerFactory.getLogger(MemoryChecker.class).debug("Enough free memory detected");
        }
        return true;
    }
}

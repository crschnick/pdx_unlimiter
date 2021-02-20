package com.crschnick.pdx_unlimiter.app.gui;

import com.crschnick.pdx_unlimiter.app.core.ComponentManager;
import com.crschnick.pdx_unlimiter.app.core.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.gui.dialog.GuiErrorReporter;
import com.crschnick.pdx_unlimiter.app.util.ThreadHelper;
import javafx.application.Platform;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

public class GuiPlatformHelper {

    private static void waitForPlatform() {
        if (Platform.isFxApplicationThread()) {
            return;
        }

        if (semaphore.hasQueuedThreads()) {
            return;
        }

        LoggerFactory.getLogger(GuiPlatformHelper.class).debug("Waiting for platform thread");
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(latch::countDown);
        try {
            if (!latch.await(7, TimeUnit.SECONDS)) {
                ErrorHandler.handleException(
                        new TimeoutException("Wait for platform thread timed out. Possible deadlock"));
            }
        } catch (InterruptedException e) {
            ErrorHandler.handleException(e);
        }
        LoggerFactory.getLogger(GuiPlatformHelper.class).debug("Synced with platform thread");
    }

    private static volatile AtomicInteger otherPauses = new AtomicInteger(0);
    private static Semaphore semaphore = new Semaphore(0);

    public static void doWhilePlatformIsPaused(Runnable r) {
        waitForAndPausePlatform();
        r.run();
        continuePlatform();
    }

    private static void waitForAndPausePlatform() {
        if (Platform.isFxApplicationThread()) {
            return;
        }

        if (semaphore.hasQueuedThreads()) {
            otherPauses.incrementAndGet();
            return;
        }

        waitForPlatform();

        LoggerFactory.getLogger(GuiPlatformHelper.class).debug("Pausing platform thread");
        Platform.runLater(() -> {
            try {
                if (!semaphore.tryAcquire(5, TimeUnit.SECONDS)) {
                    ErrorHandler.handleException(
                            new TimeoutException("Platform continue timed out. Possible deadlock"));
                }
            } catch (InterruptedException ignored) {
            }
        });
        while (semaphore.getQueueLength() == 0) {
            ThreadHelper.sleep(1);
        }
    }

    private static void continuePlatform() {
        if (Platform.isFxApplicationThread()) {
            return;
        }

        if (semaphore.hasQueuedThreads() && otherPauses.get() > 0) {
            otherPauses.decrementAndGet();
            return;
        }

        LoggerFactory.getLogger(GuiPlatformHelper.class).debug("Continuing platform thread");
        semaphore.release();
    }
}

package com.crschnick.pdx_unlimiter.gui_utils;

import com.crschnick.pdx_unlimiter.sentry_utils.ErrorHandler;
import javafx.application.Platform;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

public class GuiPlatformHelper {

    private static final AtomicInteger otherPauses = new AtomicInteger(0);
    private static final Semaphore semaphore = new Semaphore(0);

    private static void waitForPlatform() {
        if (Platform.isFxApplicationThread()) {
            return;
        }

        if (semaphore.hasQueuedThreads()) {
            return;
        }

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
    }

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
            try {
                Thread.sleep(1);
            } catch (InterruptedException ignored) {
            }
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

        semaphore.release();
    }
}

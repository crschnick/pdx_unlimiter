package com.crschnick.pdx_unlimiter.app.gui;

import com.crschnick.pdx_unlimiter.app.core.ErrorHandler;
import javafx.application.Platform;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class GuiPlatformHelper {

    public static void waitForPlatform() {
        if (Platform.isFxApplicationThread()) {
            return;
        }

        LoggerFactory.getLogger(GuiPlatformHelper.class).trace("Waiting for platform thread");
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(latch::countDown);
        try {
            if (!latch.await(5, TimeUnit.SECONDS)) {
                LoggerFactory.getLogger(GuiPlatformHelper.class).warn(
                        "Wait for platform thread timed out. Possible deadlock");
            }
        } catch (InterruptedException e) {
            ErrorHandler.handleException(e);
        }
        LoggerFactory.getLogger(GuiPlatformHelper.class).trace("Synced with platform thread");
    }
}

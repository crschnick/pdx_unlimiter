package com.crschnick.pdxu.app.platform;

import com.crschnick.pdxu.app.core.*;
import com.crschnick.pdxu.app.core.check.AppGpuCheck;
import com.crschnick.pdxu.app.core.window.AppModifiedStage;
import com.crschnick.pdxu.app.issue.TrackEvent;
import com.crschnick.pdxu.app.util.OsType;
import com.crschnick.pdxu.app.util.ThreadHelper;

import javafx.application.Application;
import javafx.application.Platform;

import lombok.Getter;
import lombok.SneakyThrows;

import java.util.concurrent.CountDownLatch;

public class PlatformInit {

    private static final CountDownLatch latch = new CountDownLatch(2);
    private static Thread loadingThread;

    @Getter
    private static Throwable error;

    public static boolean isLoadingThread() {
        return Thread.currentThread() == loadingThread || (loadingThread != null && Platform.isFxApplicationThread());
    }

    @SneakyThrows
    public static synchronized void init(boolean wait) {
        // Already finished
        if (latch.getCount() == 0) {
            if (error != null) {
                throw error;
            }

            return;
        }

        // Another thread is loading
        if (latch.getCount() == 1) {
            if (Thread.currentThread() == loadingThread) {
                return;
            }

            if (wait) {
                latch.await();
            }

            if (error != null) {
                throw error;
            }

            return;
        }

        if (latch.getCount() == 2) {
            latch.countDown();
        }

        ThreadHelper.runAsync(() -> {
            loadingThread = Thread.currentThread();
            initSync();
            loadingThread = null;
        });
        if (wait) {
            if (error != null) {
                throw error;
            }
            latch.await();
        }
    }

    private static void initSync() {
        if (AppProperties.get().isAotTrainMode() && OsType.ofLocal() == OsType.LINUX) {
            latch.countDown();
            return;
        }

        try {
            TrackEvent.info("Platform init started");
            AppModifiedStage.init();
            PlatformState.initPlatformOrThrow();
            AppGpuCheck.check();
            AppFont.init();
            PlatformThread.runLaterIfNeededBlocking(() -> {
                AppStyle.init();
                AppTheme.init();
            });
            AppI18n.init();
            AppDesktopIntegration.init();
            GlobalClipboard.init();

            // Must not be called on platform thread
            // This will not finish until the platform exits, so we use a platform thread to not lose a virtual one
            ThreadHelper.createPlatformThread("app-wait", false, () -> {
                        Application.launch(App.class);
                    })
                    .start();
            while (App.getApp() == null) {
                ThreadHelper.sleep(10);
            }
            TrackEvent.info("Platform init finished");
            latch.countDown();
        } catch (Throwable t) {
            error = t;
            latch.countDown();
        }
    }
}

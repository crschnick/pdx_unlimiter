package com.crschnick.pdxu.app.core;

import com.crschnick.pdxu.app.issue.ErrorEventFactory;
import com.crschnick.pdxu.app.issue.TrackEvent;
import com.crschnick.pdxu.app.util.ThreadHelper;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class TaskExecutor {

    private static final TaskExecutor INSTANCE = new TaskExecutor();
    private final BooleanProperty busy = new SimpleBooleanProperty(false);
    private boolean active = false;
    private ExecutorService executorService;
    private Thread thread;

    public static TaskExecutor getInstance() {
        return INSTANCE;
    }

    public void start() {
        active = true;
        executorService = Executors.newSingleThreadExecutor(
                r -> {
                    thread = ThreadHelper.createPlatformThread("Task Executor", false, r);
                    return thread;
                });
    }

    public void stopAndWait() {
        stop(null);
        try {
            // Should terminate fast
            executorService.awaitTermination(10, TimeUnit.DAYS);
            TrackEvent.debug("Task executor stopped");
        } catch (InterruptedException e) {
            ErrorEventFactory.fromThrowable(e).handle();
        }
    }

    public void stop(Runnable finalize) {
        TrackEvent.debug("Stopping task executor ...");

        active = false;

        TrackEvent.debug("Waiting for tasks to finish ...");
        executorService.submit(() -> {
            TrackEvent.debug("Performing finalizing task");
            if (finalize != null) finalize.run();
            TrackEvent.debug("Task executor finished");
        });
        executorService.shutdown();
    }

    public void submitOrRun(Runnable r) {
        if (Thread.currentThread().equals(thread)) {
            r.run();
        } else {
            submitTask(r, false);
        }
    }

    public void submitTask(Runnable r, boolean isBlocking) {
        submitTask(() -> {
            r.run();
            return null;
        }, v -> {
        }, isBlocking);
    }

    public <T> void submitTask(Callable<T> r, Consumer<T> onFinish, boolean isBlocking) {
        if (executorService.isShutdown()) {
            return;
        }

        executorService.submit(() -> {
            if (!active) {
                return;
            }

            if (isBlocking) {
                busy.setValue(true);
            }
            try {
                T v = r.call();
                onFinish.accept(v);
            } catch (Exception e) {
                ErrorEventFactory.fromThrowable(e).handle();
            }


            if (isBlocking) {
                busy.setValue(false);
            }
            ThreadHelper.sleep(50);
        });
    }

    public boolean isBusy() {
        return busy.get();
    }

    public BooleanProperty busyProperty() {
        return busy;
    }
}

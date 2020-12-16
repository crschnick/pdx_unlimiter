package com.crschnick.pdx_unlimiter.app.installation;

import com.crschnick.pdx_unlimiter.app.PdxuApp;
import com.crschnick.pdx_unlimiter.app.util.ThreadHelper;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.function.Consumer;

public class TaskExecutor {

    private static final TaskExecutor INSTANCE = new TaskExecutor();

    public void start() {
        active = true;
        executorService = Executors.newSingleThreadExecutor(
                r -> ThreadHelper.create("Task Executor", false, r));
    }

    public void stopAndWait() {
        stop(null);
        try {
            // Should terminate fast
            executorService.awaitTermination(10, TimeUnit.DAYS);
            LoggerFactory.getLogger(TaskExecutor.class).debug("Task executor stopped");
        } catch (InterruptedException e) {
            ErrorHandler.handleException(e);
        }
    }

    public void stop(Runnable finalize) {
        LoggerFactory.getLogger(TaskExecutor.class).debug("Stopping task executor ...");

        active = false;

        LoggerFactory.getLogger(TaskExecutor.class).debug("Waiting for tasks to finish ...");
        executorService.submit(() -> {
            LoggerFactory.getLogger(TaskExecutor.class).debug("Performing finalizing task");
            if (finalize != null) finalize.run();
            LoggerFactory.getLogger(TaskExecutor.class).debug("Task executor finished");
        });
        executorService.shutdown();
    }

    private boolean active = false;
    private BooleanProperty busy = new SimpleBooleanProperty(false);
    private ExecutorService executorService;

    public void submitLoop(Runnable r) {
        Runnable loopRunner = new Runnable() {
            @Override
            public void run() {
                r.run();
                if (active) {
                    ThreadHelper.sleep(10);
                    executorService.submit(this);
                }
            }
        };

        if (!executorService.isShutdown()) {
            executorService.submit(loopRunner);
        }
    }

    public void submitTask(Runnable r, boolean isBlocking) {
        submitTask(() -> {
            r.run();
            return null;
        }, v -> {}, isBlocking);
    }

    public <T> void submitTask(Callable<T> r, Consumer<T> onFinish, boolean isBlocking) {
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
                ErrorHandler.handleException(e);
            }
            if (isBlocking) {
                busy.setValue(false);
            }
        });
    }

    public static TaskExecutor getInstance() {
        return INSTANCE;
    }

    public boolean isBusy() {
        return busy.get();
    }

    public BooleanProperty busyProperty() {
        return busy;
    }
}

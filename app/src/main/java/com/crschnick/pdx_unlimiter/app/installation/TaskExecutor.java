package com.crschnick.pdx_unlimiter.app.installation;

import com.crschnick.pdx_unlimiter.app.PdxuApp;
import com.crschnick.pdx_unlimiter.app.util.ThreadHelper;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

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
        active = false;
        executorService.shutdown();

        try {
            // Should terminate fast
            // TODO: Temp solution!
            executorService.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            ErrorHandler.handleException(e);
        }
    }

    private boolean active = false;
    private BooleanProperty busy = new SimpleBooleanProperty(false);
    private ExecutorService executorService;

    public void submitTask(Runnable r) {
        submitTask(() -> {
            r.run();
            return null;
        }, v -> {});
    }

    public <T> void submitTask(Callable<T> r, Consumer<T> onFinish) {
        executorService.submit(() -> {
            if (!active) {
                return;
            }

            busy.setValue(true);
            try {
                T v = r.call();
                onFinish.accept(v);
            } catch (Exception e) {
                ErrorHandler.handleException(e);
            }
            busy.setValue(false);
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

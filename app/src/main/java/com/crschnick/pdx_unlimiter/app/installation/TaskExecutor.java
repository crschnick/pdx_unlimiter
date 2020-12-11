package com.crschnick.pdx_unlimiter.app.installation;

import com.crschnick.pdx_unlimiter.app.PdxuApp;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.concurrent.*;
import java.util.function.Consumer;

public class TaskExecutor {

    private static final TaskExecutor INSTANCE = new TaskExecutor();

    public static void start() {
        INSTANCE.active = true;
        INSTANCE.executorService = Executors.newSingleThreadExecutor();
    }

    public static void stopAndWait() {
        INSTANCE.active = false;
        INSTANCE.executorService.shutdown();

        try {
            // Should terminate fast
            INSTANCE.executorService.awaitTermination(1, TimeUnit.DAYS);
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

package com.crschnick.pdxu.app.util;

@FunctionalInterface
public interface FailableRunnable<E extends Throwable> {

    void run() throws E;
}

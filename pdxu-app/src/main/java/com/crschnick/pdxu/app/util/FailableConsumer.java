package com.crschnick.pdxu.app.util;

@FunctionalInterface
public interface FailableConsumer<T, E extends Throwable> {

    void accept(T var1) throws E;
}

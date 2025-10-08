package com.crschnick.pdxu.app.util;

@FunctionalInterface
public interface FailableFunction<T, R, E extends Throwable> {

    R apply(T var1) throws E;
}

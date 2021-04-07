package com.crschnick.pdx_unlimiter.sentry_utils;

import java.nio.file.Path;
import java.util.ServiceLoader;

public interface ErrorHandler {

    static void handleException(Throwable t) {
        ServiceLoader<ErrorHandler> loader = ServiceLoader
                .load(ErrorHandler.class);
        loader.findFirst().ifPresent(h -> h.handle(t));
    }

    static void handleException(Throwable t, Path... files) {
        ServiceLoader<ErrorHandler> loader = ServiceLoader
                .load(ErrorHandler.class);
        loader.findFirst().ifPresent(h -> h.handle(t, files));
    }

    void handle(Throwable t);

    void handle(Throwable t, Path... files);
}

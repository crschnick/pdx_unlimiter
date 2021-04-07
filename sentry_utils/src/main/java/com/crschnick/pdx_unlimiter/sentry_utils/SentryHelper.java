package com.crschnick.pdx_unlimiter.sentry_utils;

import io.sentry.ISpan;
import io.sentry.NoOpSpan;
import io.sentry.Sentry;

public class SentryHelper {

    public static ISpan startSpan(String operation) {
        if (Sentry.getSpan() != null) {
            return Sentry.getSpan().startChild(operation);
        } else {
            return NoOpSpan.getInstance();
        }
    }

    public static ISpan getSpan() {
        if (Sentry.getSpan() != null) {
            return Sentry.getSpan();
        } else {
            return NoOpSpan.getInstance();
        }
    }
}

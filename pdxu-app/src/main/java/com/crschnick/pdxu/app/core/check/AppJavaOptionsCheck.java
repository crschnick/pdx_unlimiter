package com.crschnick.pdxu.app.core.check;

import com.crschnick.pdxu.app.core.AppCache;
import com.crschnick.pdxu.app.core.AppNames;
import com.crschnick.pdxu.app.issue.ErrorEventFactory;

public class AppJavaOptionsCheck {

    public static void check() {
        if (AppCache.getBoolean("javaOptionsWarningShown", false)) {
            return;
        }

        var env = System.getenv("_JAVA_OPTIONS");
        if (env == null || env.isBlank()) {
            return;
        }

        ErrorEventFactory.fromMessage(
                        "You have configured the global environment variable _JAVA_OPTIONS=%s on your system."
                                        .formatted(env)
                                + " This will forcefully apply all custom JVM options to "
                                + AppNames.ofCurrent().getName() + " and can cause a variety of different issues."
                                + " Please remove this global environment variable and use local configuration instead for your other JVM programs.")
                .expected()
                .handle();
        AppCache.update("javaOptionsWarningShown", true);
    }
}

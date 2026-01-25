package com.crschnick.pdxu.app.core.check;

import com.crschnick.pdxu.app.core.AppCache;
import com.crschnick.pdxu.app.core.AppNames;
import com.crschnick.pdxu.app.issue.ErrorEventFactory;

public class AppJavaOptionsCheck {

    public static void check() {
        var env = System.getenv("_JAVA_OPTIONS");
        if (env == null || env.isBlank()) {
            return;
        }

        ErrorEventFactory.fromMessage(
                        "You have configured the global environment variable _JAVA_OPTIONS=%s on your system."
                                        .formatted(env)
                                + " This will forcefully apply all custom JVM options to "
                                + AppNames.ofCurrent().getName() + " and can cause a variety of different issues."
                                + " Please remove this global environment variable from your system and use a more local configuration instead for your other JVM programs if needed.")
                .expected()
                .handle();
        AppCache.update("javaOptionsWarningShown", true);
    }
}

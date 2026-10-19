package com.crschnick.pdxu.app.core.check;

import com.crschnick.pdxu.app.core.AppCache;
import com.crschnick.pdxu.app.issue.ErrorEventFactory;
import com.crschnick.pdxu.app.util.OsType;

public class AppHardwareAccelerationDisableCheck {

    public static void check() {
        if (OsType.ofLocal() != OsType.LINUX) {
            return;
        }

        var cached = AppCache.getBoolean("hardwareAccelerationDisabled", false);
        if (!cached) {
            return;
        }

        AppCache.clear("hardwareAccelerationDisabled");

        ErrorEventFactory.fromMessage(
                        "A graphics driver issue was detected and the application has been restarted. Hardware acceleration has been disabled.")
                .expected()
                .handle();
    }
}

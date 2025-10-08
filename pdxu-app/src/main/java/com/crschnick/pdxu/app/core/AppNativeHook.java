package com.crschnick.pdxu.app.core;

import com.crschnick.pdxu.app.issue.ErrorEventFactory;
import com.github.kwhat.jnativehook.GlobalScreen;
import org.apache.commons.lang3.SystemUtils;

public class AppNativeHook {
    public static void registerNativeHook() {
        try {
            if (AppProperties.get().isNativeHookEnabled() && !SystemUtils.IS_OS_MAC) {
                GlobalScreen.registerNativeHook();
            }
        } catch (Throwable ex) {
            ErrorEventFactory.fromThrowable("""
                    Unable to register native hook.
                    This might be a permissions issue with your system.
                    In-game keyboard shortcuts will be unavailable!
                    
                    Restart the Pdx-Unlimiter once the permission issues are fixed to enable in-game shortcuts.""", ex).handle();
        }
    }

    public static void unregisterNativeHook() {
        try {
            if (AppProperties.get().isNativeHookEnabled() && !SystemUtils.IS_OS_MAC) {
                GlobalScreen.unregisterNativeHook();
            }
        } catch (Throwable ex) {
            ErrorEventFactory.fromThrowable("Unable to unregister native hook.\n" +
                    "This might be a permissions issue with your system.", ex).handle();
        }
    }


}

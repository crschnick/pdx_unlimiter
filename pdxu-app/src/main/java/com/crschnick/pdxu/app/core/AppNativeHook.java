package com.crschnick.pdxu.app.core;

import com.crschnick.pdxu.app.issue.ErrorEventFactory;

import com.crschnick.pdxu.app.prefs.AppPrefs;
import com.crschnick.pdxu.app.util.OsType;
import com.github.kwhat.jnativehook.GlobalScreen;
import lombok.Getter;
import org.apache.commons.lang3.SystemUtils;

public class AppNativeHook {

    @Getter
    private static final boolean enabled = AppProperties.get().isNativeHookEnabled() && AppPrefs.get().enableKeyboardShortcuts().getValue() && OsType.ofLocal() != OsType.MACOS;

    public static void registerNativeHook() {
        try {
            if (enabled) {
                GlobalScreen.registerNativeHook();
            }
        } catch (Throwable ex) {
            ErrorEventFactory.fromThrowable(
                            """
                    Unable to register native hook.
                    This might be a permissions issue with your system.
                    In-game keyboard shortcuts will be unavailable!

                    Restart the Pdx-Unlimiter once the permission issues are fixed to enable in-game shortcuts.""",
                            ex)
                    .expected()
                    .handle();
        }
    }

    public static void unregisterNativeHook() {
        try {
            if (enabled) {
                GlobalScreen.unregisterNativeHook();
            }
        } catch (Throwable ex) {
            ErrorEventFactory.fromThrowable(
                            "Unable to unregister native hook.\n"
                                    + "This might be a permissions issue with your system.",
                            ex)
                    .expected()
                    .handle();
        }
    }
}

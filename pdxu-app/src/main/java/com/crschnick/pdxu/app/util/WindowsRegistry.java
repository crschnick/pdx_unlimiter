package com.crschnick.pdxu.app.util;

import com.crschnick.pdxu.app.issue.ErrorEventFactory;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.Advapi32;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinReg;
import com.sun.jna.win32.W32APIOptions;
import lombok.Value;

import java.util.*;

public abstract class WindowsRegistry {

    public static final int HKEY_CURRENT_USER = 0x80000001;
    public static final int HKEY_LOCAL_MACHINE = 0x80000002;

    public static void init() {
        if (OsType.ofLocal() != OsType.WINDOWS) {
            return;
        }

        // Load lib
        Local.isLibrarySupported();
    }

    public static WindowsRegistry.Local of() {
        return new Local();
    }

    public abstract boolean keyExists(int hkey, String key);

    public abstract List<String> listSubKeys(int hkey, String key);

    public abstract boolean valueExists(int hkey, String key, String valueName);

    public abstract OptionalInt readIntegerValueIfPresent(int hkey, String key, String valueName);

    public abstract Optional<String> readStringValueIfPresent(int hkey, String key, String valueName);

    public Optional<String> readStringValueIfPresent(int hkey, String key) {
        return readStringValueIfPresent(hkey, key, null);
    }

    @Value
    public static class Key {
        int hkey;
        String key;
    }

    public static class Local extends WindowsRegistry {

        private WinReg.HKEY hkey(int hkey) {
            return hkey == HKEY_LOCAL_MACHINE ? WinReg.HKEY_LOCAL_MACHINE : WinReg.HKEY_CURRENT_USER;
        }

        private static Boolean libraryLoaded;

        private static synchronized boolean isLibrarySupported() {
            if (libraryLoaded != null) {
                return libraryLoaded;
            }

            try {
                Native.load("Advapi32", Advapi32.class, W32APIOptions.DEFAULT_OPTIONS);
                return (libraryLoaded = true);
            } catch (Throwable t) {
                libraryLoaded = false;
                ErrorEventFactory.fromThrowable(t)
                        .description("Unable to load native library Advapi32.dll for registry queries."
                                + " Registry queries will fail and some functionality will be unavailable")
                        .handle();
                return false;
            }
        }

        @Override
        public boolean keyExists(int hkey, String key) {
            if (!isLibrarySupported()) {
                return false;
            }

            try {
                return Advapi32Util.registryKeyExists(hkey(hkey), key);
            } catch (Win32Exception ignored) {
                return false;
            }
        }

        @Override
        public List<String> listSubKeys(int hkey, String key) {
            if (!isLibrarySupported()) {
                return List.of();
            }

            try {
                return Arrays.asList(Advapi32Util.registryGetKeys(hkey(hkey), key));
            } catch (Win32Exception ignored) {
                return List.of();
            }
        }

        @Override
        public boolean valueExists(int hkey, String key, String valueName) {
            if (!isLibrarySupported()) {
                return false;
            }

            try {
                return Advapi32Util.registryValueExists(hkey(hkey), key, valueName);
            } catch (Win32Exception ignored) {
                return false;
            }
        }

        @Override
        public OptionalInt readIntegerValueIfPresent(int hkey, String key, String valueName) {
            if (!isLibrarySupported()) {
                return OptionalInt.empty();
            }

            try {
                if (!Advapi32Util.registryValueExists(hkey(hkey), key, valueName)) {
                    return OptionalInt.empty();
                }

                return OptionalInt.of(Advapi32Util.registryGetIntValue(hkey(hkey), key, valueName));
            } catch (Win32Exception ignored) {
                return OptionalInt.empty();
            }
        }

        @Override
        public Optional<String> readStringValueIfPresent(int hkey, String key, String valueName) {
            if (!isLibrarySupported()) {
                return Optional.empty();
            }

            try {
                if (!Advapi32Util.registryValueExists(hkey(hkey), key, valueName)) {
                    return Optional.empty();
                }

                return Optional.ofNullable(Advapi32Util.registryGetStringValue(hkey(hkey), key, valueName));
            } catch (Win32Exception ignored) {
                return Optional.empty();
            }
        }
    }
}

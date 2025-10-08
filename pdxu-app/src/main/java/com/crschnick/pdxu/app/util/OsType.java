package com.crschnick.pdxu.app.util;

import java.util.Locale;

public sealed interface OsType permits OsType.Windows, OsType.Linux, OsType.MacOs {

    Windows WINDOWS = new Windows();
    Linux LINUX = new Linux();
    MacOs MACOS = new MacOs();

    static OsType ofLocal() {
        String osName = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
        if ((osName.contains("mac")) || (osName.contains("darwin"))) {
            return MACOS;
        } else if (osName.contains("win")) {
            return WINDOWS;
        } else {
            return LINUX;
        }
    }

    String getId();

    String getName();

    final class Windows implements OsType {

        @Override
        public String getName() {
            return "Windows";
        }

        @Override
        public String getId() {
            return "windows";
        }
    }

    final class Linux implements OsType {

        @Override
        public String getName() {
            return "Linux";
        }

        @Override
        public String getId() {
            return "linux";
        }
    }

    final class MacOs implements OsType {

        @Override
        public String getId() {
            return "macos";
        }

        @Override
        public String getName() {
            return "Mac";
        }
    }
}

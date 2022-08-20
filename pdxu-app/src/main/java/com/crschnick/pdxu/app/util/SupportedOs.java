package com.crschnick.pdxu.app.util;

import org.apache.commons.lang3.SystemUtils;

public enum SupportedOs {

    WINDOWS,
    LINUX,
    MAC;

    public static SupportedOs get() {
        if (SystemUtils.IS_OS_WINDOWS) {
            return WINDOWS;
        } else if (SystemUtils.IS_OS_LINUX) {
            return LINUX;
        } else if (SystemUtils.IS_OS_MAC) {
            return MAC;
        } else {
            throw new UnsupportedOperationException("Unsupported operating system");
        }
    }
}

package com.crschnick.pdx_unlimiter.app.util;

import org.apache.commons.lang3.SystemUtils;

import java.nio.file.Path;
import java.nio.file.Paths;

public class OsHelper {

    public static Path getUserDocumentsPath() {
        if (SystemUtils.IS_OS_WINDOWS) {
            return Path.of(System.getenv("USER_DOCUMENTS"));
        } else {
            return Paths.get(System.getProperty("user.home"), ".local", "share");
        }
    }
}

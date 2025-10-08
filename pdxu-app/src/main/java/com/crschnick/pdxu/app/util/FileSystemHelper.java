package com.crschnick.pdxu.app.util;

import com.crschnick.pdxu.app.core.AppSystemInfo;

import java.nio.file.Path;

public class FileSystemHelper {

    public static Path getUserDocumentsPath() {
        return switch (OsType.ofLocal()) {
            case OsType.Linux linux -> AppSystemInfo.ofLinux().getUserHome().resolve(".local", "share");
            case OsType.MacOs macOs -> AppSystemInfo.ofMacOs().getUserHome().resolve("Library", "Application Support");
            case OsType.Windows windows -> AppSystemInfo.ofWindows().getUserHome().resolve("Documents");
        };
    }

    public static String getFileSystemCompatibleName(String name) {
        return name.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
}

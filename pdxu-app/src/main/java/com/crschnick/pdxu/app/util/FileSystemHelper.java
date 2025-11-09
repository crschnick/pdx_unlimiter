package com.crschnick.pdxu.app.util;

import com.crschnick.pdxu.app.core.AppSystemInfo;

import java.nio.file.Path;

public class FileSystemHelper {

    private static Path paradoxDocumentsPath;
    private static Path userDocumentsPath;

    public static Path getParadoxDocumentsPath() {
        if (paradoxDocumentsPath != null) {
            return paradoxDocumentsPath;
        }

        paradoxDocumentsPath = switch (OsType.ofLocal()) {
            case OsType.Linux linux -> AppSystemInfo.ofLinux().getUserHome().resolve(".local", "share");
            case OsType.MacOs macOs -> AppSystemInfo.ofMacOs().getUserHome().resolve("Documents");
            case OsType.Windows windows -> AppSystemInfo.ofWindows().getDocuments();
        };

        return paradoxDocumentsPath;
    }

    public static Path getUserDataBasePath() {
        if (userDocumentsPath != null) {
            return userDocumentsPath;
        }

        userDocumentsPath = switch (OsType.ofLocal()) {
            case OsType.Linux linux -> AppSystemInfo.ofLinux().getUserHome().resolve(".local", "share");
            case OsType.MacOs macOs -> AppSystemInfo.ofMacOs().getUserHome().resolve("Library", "Application Support");
            case OsType.Windows windows -> AppSystemInfo.ofWindows().getDocuments();
        };

        return userDocumentsPath;
    }

    public static String getFileSystemCompatibleName(String name) {
        return name.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
}

package com.crschnick.pdxu.app.util;

import org.apache.commons.lang3.SystemUtils;

import javax.swing.filechooser.FileSystemView;
import java.nio.file.Path;
import java.nio.file.Paths;

public class OsHelper {

    public static String getFileSystemCompatibleName(String name) {
        return name.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    public static Path getUserDocumentsPath() {
        switch (SupportedOs.get()) {
            case WINDOWS -> {
                var dir = FileSystemView.getFileSystemView().getDefaultDirectory();

                // Sometimes this is null. Why?
                if (dir == null) {
                    return Paths.get(System.getProperty("user.home"));
                }

                return dir.toPath();
            }
            case LINUX -> {
                return Paths.get(System.getProperty("user.home"), ".local", "share");
            }
            case MAC -> {
                return Paths.get(System.getProperty("user.home"), "Library", "Application Support");
            }
        }

        throw new AssertionError();
    }
}

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
        if (SystemUtils.IS_OS_WINDOWS) {
            var dir = FileSystemView.getFileSystemView().getDefaultDirectory();

            // Sometimes this is null. Why?
            if (dir == null) {
                return Paths.get(System.getProperty("user.home"));
            }

            return dir.toPath();
        } else {
            return Paths.get(System.getProperty("user.home"), ".local", "share");
        }
    }
}

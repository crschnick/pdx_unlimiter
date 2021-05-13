package com.crschnick.pdxu.app.util;

import org.apache.commons.lang3.SystemUtils;

import javax.swing.filechooser.FileSystemView;
import java.nio.file.Path;
import java.nio.file.Paths;

public class OsHelper {

    public static Path getUserDocumentsPath() {
        if (SystemUtils.IS_OS_WINDOWS) {
            return Path.of(FileSystemView.getFileSystemView().getDefaultDirectory().getPath());
        } else {
            return Paths.get(System.getProperty("user.home"), ".local", "share");
        }
    }
}

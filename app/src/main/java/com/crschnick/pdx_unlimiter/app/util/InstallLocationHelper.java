package com.crschnick.pdx_unlimiter.app.util;

import com.crschnick.pdx_unlimiter.app.core.ErrorHandler;
import org.apache.commons.lang3.SystemUtils;

import javax.swing.filechooser.FileSystemView;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class InstallLocationHelper {

    public static Path getUserDocumentsPath() {
        if (SystemUtils.IS_OS_WINDOWS) {
            return Path.of(FileSystemView.getFileSystemView().getDefaultDirectory().getPath());
        } else {
            return Paths.get(System.getProperty("user.home"), ".local", "share");
        }
    }

    public static Optional<Path> getInstallPath(String app) {
        Optional<Path> steamDir = SteamHelper.getSteamPath();
        var installDir = steamDir.map(d -> d.resolve("steamapps")
                .resolve("common").resolve(app))
                .filter(Files::exists);
        if (installDir.isPresent()) {
            try {
                return Optional.of(installDir.get().toRealPath());
            } catch (IOException e) {
                ErrorHandler.handleException(e);
            }
        }
        return Optional.empty();
    }
}

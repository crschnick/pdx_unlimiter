package com.crschnick.pdx_unlimiter.app.util;

import com.crschnick.pdx_unlimiter.app.installation.ErrorHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Optional;

public class InstallLocationHelper {

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

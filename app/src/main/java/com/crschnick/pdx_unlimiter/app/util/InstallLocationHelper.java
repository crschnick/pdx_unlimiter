package com.crschnick.pdx_unlimiter.app.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class InstallLocationHelper {

    public static Optional<Path> getInstallPath(String app) {
        Optional<Path> steamDir = SteamHelper.getSteamPath();
        return steamDir.map(d -> d.resolve("steamapps").resolve("common").resolve(app)).filter(Files::exists);
    }
}

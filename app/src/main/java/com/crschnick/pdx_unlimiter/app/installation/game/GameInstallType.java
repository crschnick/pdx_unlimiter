package com.crschnick.pdx_unlimiter.app.installation.game;

import org.apache.commons.lang3.SystemUtils;

import java.nio.file.Path;

public interface GameInstallType {

    Path getExecutable(Path p);

    public Path getSteamAppIdFile(Path p);

    public Path getLauncherDataPath(Path p);

    public Path getModBasePath(Path p);

    public static class StandardInstallType implements GameInstallType {

        private final String executableName;

        public StandardInstallType(String executableName) {
            this.executableName = executableName;
        }

        @Override
        public Path getExecutable(Path p) {
            return p.resolve(executableName + (SystemUtils.IS_OS_WINDOWS ? ".exe" : ""));
        }

        public Path getSteamAppIdFile(Path p) {
            return p.resolve("steam_appid.txt");
        }

        public Path getLauncherDataPath(Path p) {
            return p;
        }

        public Path getModBasePath(Path p) {
            return p;
        }
    }

    public static class Ck3InstallType extends StandardInstallType {

        public Ck3InstallType() {
            super("ck3");
        }

        public Path getSteamAppIdFile(Path p) {
            return p.resolve("binaries").resolve("steam_appid.txt");
        }

        public Path getLauncherDataPath(Path p) {
            return p.resolve("launcher");
        }

        public Path getModBasePath(Path p) {
            return p.resolve("game");
        }
    }
}

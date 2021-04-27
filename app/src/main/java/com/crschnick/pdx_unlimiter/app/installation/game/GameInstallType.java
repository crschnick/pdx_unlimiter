package com.crschnick.pdx_unlimiter.app.installation.game;

import org.apache.commons.lang3.SystemUtils;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public interface GameInstallType {

    GameInstallType EU4 = new StandardInstallType("eu4") {
        @Override
        public List<String> getLaunchArguments() {
            return List.of("-continuelastsave");
        }
    };

    GameInstallType HOI4 = new StandardInstallType("hoi4") {
        @Override
        public List<String> getLaunchArguments() {
            return List.of("-gdpr-compliant", "--continuelastsave");
        }

        @Override
        public Optional<String> debugModeSwitch() {
            return Optional.of("-debug");
        }
    };

    GameInstallType STELLARIS = new StandardInstallType("stellaris") {
        @Override
        public List<String> getLaunchArguments() {
            return List.of("-gdpr-compliant", "--continuelastsave");
        }
    };

    GameInstallType CK3 = new StandardInstallType("binaries/ck3") {
        @Override
        public Optional<String> debugModeSwitch() {
            return Optional.of("-debug_mode");
        }

        @Override
        public List<String> getLaunchArguments() {
            return List.of("-gdpr-compliant", "--continuelastsave");
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
    };

    GameInstallType CK2 = new StandardInstallType("CK2game") {
        @Override
        public Optional<String> debugModeSwitch() {
            return Optional.of("-debug_mode");
        }

        @Override
        public List<String> getLaunchArguments() {
            return List.of();
        }
    };

    List<String> getLaunchArguments();

    Path getExecutable(Path p);

    public default Path getSteamAppIdFile(Path p) {
        return p.resolve("steam_appid.txt");
    }

    public default Path getLauncherDataPath(Path p) {
        return p;
    }

    public default Path getModBasePath(Path p) {
        return p;
    }

    public default Optional<String> debugModeSwitch() {
        return Optional.empty();
    }

    public static abstract class StandardInstallType implements GameInstallType {

        private final String executableName;

        public StandardInstallType(String executableName) {
            this.executableName = executableName;
        }

        @Override
        public Path getExecutable(Path p) {
            return p.resolve(executableName + (SystemUtils.IS_OS_WINDOWS ? ".exe" : ""));
        }
    }
}

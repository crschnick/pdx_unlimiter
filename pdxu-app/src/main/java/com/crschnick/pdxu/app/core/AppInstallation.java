package com.crschnick.pdxu.app.core;

import com.crschnick.pdxu.app.util.OsType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class AppInstallation {

    private static final Windows WINDOWS = AppProperties.get().isImage()
            ? new Windows(determineCurrentInstallationBasePath())
            : new WindowsDev(determineDefaultInstallationBasePath(), determineCurrentInstallationBasePath());
    private static final Linux LINUX = AppProperties.get().isImage()
            ? new Linux(determineCurrentInstallationBasePath())
            : new LinuxDev(determineDefaultInstallationBasePath(), determineCurrentInstallationBasePath());
    private static final MacOs MACOS = AppProperties.get().isImage()
            ? new MacOs(determineCurrentInstallationBasePath())
            : new MacOsDev(determineDefaultInstallationBasePath(), determineCurrentInstallationBasePath());
    private final Path base;

    private AppInstallation(Path base) {
        this.base = base;
    }

    public static AppInstallation ofCurrent() {
        return switch (OsType.ofLocal()) {
            case OsType.Windows ignored -> WINDOWS;
            case OsType.Linux ignored -> LINUX;
            case OsType.MacOs ignored -> MACOS;
        };
    }

    public static AppInstallation ofDefault() {
        var def = determineDefaultInstallationBasePath();
        return switch (OsType.ofLocal()) {
            case OsType.Windows ignored -> new Windows(def);
            case OsType.Linux ignored -> new Linux(def);
            case OsType.MacOs ignored -> new MacOs(def);
        };
    }

    private static Path determineDefaultInstallationBasePath() {
        return switch (OsType.ofLocal()) {
            case OsType.Linux ignored -> Path.of("/opt/" + AppNames.ofCurrent().getKebapName());
            case OsType.MacOs ignored ->
                Path.of("/Applications/" + AppNames.ofCurrent().getName() + ".app");
            case OsType.Windows ignored -> {
                var pg = AppSystemInfo.ofWindows().getProgramFiles();
                var systemPath = pg.resolve(AppNames.ofCurrent().getName());
                if (Files.exists(systemPath)) {
                    yield systemPath;
                }

                var ad = AppSystemInfo.ofWindows().getLocalAppData();
                yield ad.resolve(AppNames.ofCurrent().getName());
            }
        };
    }

    private static Path determineCurrentInstallationBasePath() {
        var command = ProcessHandle.current().info().command();
        // We should always have a command associated with the current process, otherwise something went seriously wrong
        if (command.isEmpty()) {
            var javaHome = System.getProperty("java.home");
            var javaExec = toRealPathIfPossible(Path.of(javaHome, "bin", "java"));
            var path = getInstallationBasePathForJavaExecutable(javaExec);
            return path;
        }

        // Resolve any possible links to a real path
        Path path = toRealPathIfPossible(Path.of(command.get()));
        // Check if the process was started using a relative path, and adapt it if necessary
        if (!path.isAbsolute()) {
            path = toRealPathIfPossible(Path.of(System.getProperty("user.dir")).resolve(path));
        }

        var name = path.getFileName().toString();
        // Check if we launched the JVM via a start script instead of the native executable
        if (name.endsWith("java") || name.endsWith("java.exe")) {
            // If we are not an image, we are probably running in a development environment where we want to use the
            // working directory
            var isImage = AppProperties.get().isImage();
            if (!isImage) {
                return Path.of(System.getProperty("user.dir"));
            }
            return getInstallationBasePathForJavaExecutable(path);
        } else {
            return getInstallationBasePathForDaemonExecutable(path);
        }
    }

    private static Path getInstallationBasePathForDaemonExecutable(Path executable) {
        // Resolve root path of installation relative to executable in a JPackage installation
        return switch (OsType.ofLocal()) {
            case OsType.Linux ignored -> executable.getParent().getParent();
            case OsType.MacOs ignored -> executable.getParent().getParent().getParent();
            case OsType.Windows ignored -> executable.getParent();
        };
    }

    private static Path getInstallationBasePathForJavaExecutable(Path executable) {
        // Resolve root path of installation relative to executable in a JPackage installation
        return switch (OsType.ofLocal()) {
            case OsType.Linux ignored ->
                executable.getParent().getParent().getParent().getParent();
            case OsType.MacOs ignored ->
                executable
                        .getParent()
                        .getParent()
                        .getParent()
                        .getParent()
                        .getParent()
                        .getParent();
            case OsType.Windows ignored -> executable.getParent().getParent().getParent();
        };
    }

    private static Path toRealPathIfPossible(Path p) {
        try {
            // Under certain conditions, e.g. when running on a ramdisk, path resolution might fail.
            // This is however not a big problem in that case, so we ignore it
            return p.toRealPath();
        } catch (IOException e) {
            return p;
        }
    }

    public Path getBaseInstallationPath() {
        return base;
    }

    public abstract Path getDebugScriptPath();

    public abstract Path getRakalyExecutable();

    public abstract Path getLangPath();

    public abstract Path getExecutablePath();

    public abstract Path getLogoPath();

    public static class Windows extends AppInstallation {

        private Windows(Path base) {
            super(base);
        }

        @Override
        public Path getDebugScriptPath() {
            return getBaseInstallationPath()
                    .resolve("scripts", AppNames.ofCurrent().getExecutableName() + "_debug.bat");
        }

        @Override
        public Path getRakalyExecutable() {
            return getBaseInstallationPath().resolve("rakaly", "rakaly.exe");
        }

        @Override
        public Path getLangPath() {
            return getBaseInstallationPath().resolve("lang");
        }

        @Override
        public Path getExecutablePath() {
            return getBaseInstallationPath().resolve(AppNames.ofCurrent().getExecutableName() + ".exe");
        }

        @Override
        public Path getLogoPath() {
            return getBaseInstallationPath().resolve("logo.ico");
        }
    }

    public static class WindowsDev extends Windows {

        private final Path devBase;

        private WindowsDev(Path base, Path devBase) {
            super(base);
            this.devBase = devBase;
        }

        @Override
        public Path getRakalyExecutable() {
            return devBase.resolve("rakaly", "rakaly_windows.exe");
        }

        @Override
        public Path getLangPath() {
            return devBase.resolve("lang");
        }

        @Override
        public Path getLogoPath() {
            return devBase.resolve("dist").resolve("logo").resolve("logo.ico");
        }
    }

    public static class Linux extends AppInstallation {

        private Linux(Path base) {
            super(base);
        }

        @Override
        public Path getDebugScriptPath() {
            return getBaseInstallationPath()
                    .resolve("scripts", AppNames.ofCurrent().getExecutableName() + "_debug.sh");
        }

        @Override
        public Path getRakalyExecutable() {
            return getBaseInstallationPath().resolve("rakaly", "rakaly");
        }

        @Override
        public Path getLangPath() {
            return getBaseInstallationPath().resolve("lang");
        }

        @Override
        public Path getExecutablePath() {
            return getBaseInstallationPath().resolve("bin", AppNames.ofCurrent().getExecutableName());
        }

        @Override
        public Path getLogoPath() {
            if (!AppProperties.get().isImage()) {
                return getBaseInstallationPath().resolve("dist").resolve("logo").resolve("logo.png");
            }

            return getBaseInstallationPath().resolve("logo.png");
        }
    }

    public static class LinuxDev extends Linux {

        private final Path devBase;

        private LinuxDev(Path base, Path devBase) {
            super(base);
            this.devBase = devBase;
        }

        @Override
        public Path getRakalyExecutable() {
            return devBase.resolve("rakaly", "rakaly_linux");
        }

        @Override
        public Path getLangPath() {
            return devBase.resolve("lang");
        }
    }

    public static class MacOs extends AppInstallation {

        private MacOs(Path base) {
            super(base);
        }

        @Override
        public Path getDebugScriptPath() {
            return getBaseInstallationPath()
                    .resolve(
                            "Contents",
                            "Resources",
                            "scripts",
                            AppNames.ofCurrent().getExecutableName() + "_debug.sh");
        }

        @Override
        public Path getRakalyExecutable() {
            return getBaseInstallationPath().resolve("Contents", "MacOS", "rakaly");
        }

        @Override
        public Path getLangPath() {
            return getBaseInstallationPath().resolve("Contents", "Resources", "lang");
        }

        @Override
        public Path getExecutablePath() {
            return getBaseInstallationPath()
                    .resolve("Contents", "MacOS", AppNames.ofCurrent().getExecutableName());
        }

        @Override
        public Path getLogoPath() {
            return getBaseInstallationPath()
                    .resolve("Contents")
                    .resolve("Resources")
                    .resolve("logo.icns");
        }
    }

    public static class MacOsDev extends MacOs {

        private final Path devBase;

        private MacOsDev(Path base, Path devBase) {
            super(base);
            this.devBase = devBase;
        }

        @Override
        public Path getRakalyExecutable() {
            return devBase.resolve("rakaly", "rakaly_mac");
        }

        @Override
        public Path getLangPath() {
            return devBase.resolve("lang");
        }

        @Override
        public Path getLogoPath() {
            return devBase.resolve("dist").resolve("logo").resolve("logo.icns");
        }
    }
}

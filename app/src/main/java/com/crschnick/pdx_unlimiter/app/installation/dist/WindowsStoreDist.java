package com.crschnick.pdx_unlimiter.app.installation.dist;

import com.crschnick.pdx_unlimiter.app.core.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.installation.Game;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.regex.Pattern;

public final class WindowsStoreDist extends PdxLauncherDist {

    private static final Pattern PACKAGE_FAMILY_NAME_PATTERN = Pattern.compile("^PackageFamilyName\\s+:\\s*([^\\s]*)$");
    private static final Pattern LOCATION_PATTERN = Pattern.compile("^InstallLocation\\s+:\\s*(.+)$");
    private final String packageFamilyName;

    public WindowsStoreDist(Game g, String packageFamilyName, Path installLocation) {
        super(g, "Windows Store", installLocation);
        this.packageFamilyName = packageFamilyName;
    }

    public static Optional<GameDist> getDist(Game g, Path dir) {
        if (g.getWindowsStoreName() == null) {
            return Optional.empty();
        }

        try {
            // Important: Use out-string to increase output width. Otherwise long file paths can be wrapped!
            var proc = new ProcessBuilder("powershell.exe", "Get-AppxPackage",
                    g.getWindowsStoreName(), "|", "out-string", "-width", "1000")
                    .start();
            var in = new String(proc.getInputStream().readAllBytes());
            proc.waitFor();
            int exit = proc.exitValue();
            if (exit != 0) {
                return Optional.empty();
            }

            var loc = in.lines().map(line -> {
                var m = LOCATION_PATTERN.matcher(line);
                return m.matches() ? Optional.of(Path.of(m.group(1))) : Optional.<Path>empty();
            }).flatMap(Optional::stream).findAny();
            var pkgFamName = in.lines().map(line -> {
                var m = PACKAGE_FAMILY_NAME_PATTERN.matcher(line);
                return m.matches() ? Optional.of(m.group(1)) : Optional.<String>empty();
            }).flatMap(Optional::stream).findAny();
            if (loc.isPresent() && pkgFamName.isPresent()) {
                // Check whether the already set install location is a windows store dist
                if (dir != null && !dir.equals(loc.get())) {
                    return Optional.empty();
                }

                return Optional.of(new WindowsStoreDist(g, pkgFamName.get(), loc.get()));
            }
        } catch (Exception e) {
            ErrorHandler.handleException(e);
        }
        return Optional.empty();
    }

    @Override
    protected Path getLauncherSettings() {
        return getGame().getInstallType().getWindowsStoreLauncherDataPath(
                getInstallLocation()).resolve("launcher-settings.json");
    }

    @Override
    public Path getIcon() {
        return getGame().getInstallType().getWindowsStoreIcon(getInstallLocation());
    }

    @Override
    public boolean isGameInstance(String cmd) {
        var execName = getGame().getInstallType().getExecutable(getInstallLocation()).getFileName().toString();
        return cmd.contains("\\\\?\\Volume") && cmd.contains(execName);
    }

    @Override
    public void startLauncher() throws IOException {
        new ProcessBuilder("cmd", "/C", "start", "shell:AppsFolder\\" + packageFamilyName + "!App").start();
    }

    @Override
    public boolean supportsLauncher() {
        return true;
    }

    @Override
    public boolean supportsDirectLaunch() {
        return false;
    }
}

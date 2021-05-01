package com.crschnick.pdx_unlimiter.app.installation.dist;

import com.crschnick.pdx_unlimiter.app.core.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.installation.Game;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.regex.Pattern;

public final class WindowsStoreDist extends PdxLauncherDist {

    private static final Pattern PACKAGE_FAMILY_NAME_PATTERN = Pattern.compile("^PackageFamilyName\\s+:\\s*([^\\s]*)$");
    private static final Pattern LOCATION_PATTERN = Pattern.compile("^InstallLocation\\s+:\\s*([^\\s]*)$");

    public static Optional<GameDist> getDist(Game g, Path dir) {
        if (g.getWindowsStoreName() == null) {
            return Optional.empty();
        }

        try {
            var proc = new ProcessBuilder("powershell.exe", "Get-AppxPackage", g.getWindowsStoreName())
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
                return Optional.of(new WindowsStoreDist(g, pkgFamName.get(), loc.get()));
            }
        } catch (Exception e) {
            ErrorHandler.handleException(e);
        }
        return Optional.empty();
    }

    private final String packageFamilyName;

    public WindowsStoreDist(Game g, String packageFamilyName, Path installLocation) {
        super(g, "Windows Store", installLocation);
        this.packageFamilyName = packageFamilyName;
    }

    @Override
    public void startLauncher() throws IOException {
        new ProcessBuilder("start", "shell:AppsFolder\\" + packageFamilyName + "!App").start();
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

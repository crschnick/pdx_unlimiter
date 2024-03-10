package com.crschnick.pdxu.app.installation.dist;

import com.crschnick.pdxu.app.installation.Game;
import org.apache.commons.lang3.SystemUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
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

        if (!SystemUtils.IS_OS_WINDOWS) {
            return Optional.empty();
        }

        // Prevent querying for appx-package if we can rule out that the given dir is a windows store dist
        if (dir != null && !dir.toString().contains("WindowsApps")) {
            return Optional.empty();
        }

        try {
            // This is somehow very slow. Why?
            // Important: Use out-string to increase output width. Otherwise long file paths can be wrapped!
            var proc = new ProcessBuilder("powershell.exe", "Get-AppxPackage",
                    g.getWindowsStoreName(), "|", "out-string", "-width", "1000")
                    .redirectError(ProcessBuilder.Redirect.DISCARD)
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
            // Ignore exception, usually caused by not finding powershell.exe.
            // Probably because a messed up PATH?
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
    public Optional<ProcessHandle> getGameInstance(List<ProcessHandle> processes) {
        var execName = getExecutable().getFileName().toString();
        Optional<ProcessHandle> process = Optional.empty();
        for (var ph : processes) {
            var cmd = ph.info().command().orElse(null);
            if (cmd != null) {
                if (cmd.contains("\\\\?\\Volume") && cmd.contains(execName)) {
                    process = Optional.of(ph);
                    break;
                }
            }
        }
        return process;
    }

    @Override
    public void startLauncher(Map<String,String> env) throws IOException {
        var pb = new ProcessBuilder(
                "cmd", "/C", "start", "shell:AppsFolder\\" + packageFamilyName + "!App");
        pb.environment().putAll(env);
        pb.start();
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

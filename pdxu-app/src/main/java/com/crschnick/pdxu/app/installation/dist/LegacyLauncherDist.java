package com.crschnick.pdxu.app.installation.dist;

import com.crschnick.pdxu.app.installation.Game;
import org.apache.commons.lang3.SystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

public class LegacyLauncherDist extends GameDist {

    public LegacyLauncherDist(Game game, Path installLocation) {
        super(game, "Legacy", installLocation);
    }

    public static Optional<GameDist> getDist(Game g, Path dir) {
        if (dir == null) {
            return Optional.empty();
        }

        var legacyLauncher = g.getInstallType().getLegacyLauncherExecutable(dir);
        if (legacyLauncher.isEmpty() || !Files.exists(legacyLauncher.get())) {
            return Optional.empty();
        }

        return Optional.of(new LegacyLauncherDist(g, dir));
    }

    @Override
    public void startLauncher(Map<String,String> env) throws IOException {
        var exec = getGame().getInstallType().getLegacyLauncherExecutable(getInstallLocation()).orElseThrow();
        var input = new ArrayList<String>();
        // Make UAC popup if needed
        if (SystemUtils.IS_OS_WINDOWS) {
            input.add("cmd");
            input.add("/C");
        }
        input.add(exec.toString());
        var pb = new ProcessBuilder(input);
        pb.environment().putAll(env);
        pb.start();
    }

    @Override
    public boolean supportsLauncher() {
        return true;
    }

    @Override
    public boolean supportsDirectLaunch() {
        return true;
    }
}

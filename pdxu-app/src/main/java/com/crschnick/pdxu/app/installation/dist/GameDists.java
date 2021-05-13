package com.crschnick.pdxu.app.installation.dist;

import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.app.util.OsHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.apache.commons.lang3.SystemUtils;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

public class GameDists {

    private static final List<BiFunction<Game, Path, Optional<GameDist>>> COMPOUND_TYPES = List.of(
            WindowsStoreDist::getDist,
            SteamDist::getDist);

    private static final List<BiFunction<Game, Path, Optional<GameDist>>> BASIC_DISTS = List.of(
            PdxLauncherDist::getDist,
            LegacyLauncherDist::getDist,
            NoLauncherDist::getDist
    );

    private static final List<Path> installDirSearchPaths = new ArrayList<>();

    static {
        if (SystemUtils.IS_OS_WINDOWS) {
            for (var root : FileSystems.getDefault().getRootDirectories()) {
                installDirSearchPaths.add(root.resolve("Program Files (x86)"));
            }
        } else {
            installDirSearchPaths.add(OsHelper.getUserDocumentsPath().resolve("Paradox Interactive"));
        }
    }

    public static GameDist detectDist(Game g, Path dir) {
        return getCompoundDistFromDirectory(g, dir)
                .or(() -> GameDists.getBasicDistFromDirectory(g, dir))
                .orElse(new CatchAllDist(g, dir));
    }

    public static GameDist detectDist(Game g, JsonNode n) {
        if (n != null) {
            return detectDist(g, Path.of(n.textValue()));
        } else {
            return detectDist(g, (Path) null);
        }
    }

    static Optional<GameDist> getBasicDistFromDirectory(Game g, Path dir) {
        for (var e : BASIC_DISTS) {
            var r = e.apply(g, dir);
            if (r.isPresent()) {
                return r;
            }
        }
        return Optional.empty();
    }

    private static Optional<GameDist> getCompoundDistFromDirectory(Game g, Path dir) {
        for (var e : COMPOUND_TYPES) {
            var r = e.apply(g, dir);
            if (r.isPresent()) {
                return r;
            }
        }
        return Optional.empty();
    }

    public static JsonNode toNode(GameDist d) {
        return new TextNode(d.getInstallLocation().toString());
    }

    public static List<Path> getInstallDirSearchPaths() {
        return installDirSearchPaths;
    }
}

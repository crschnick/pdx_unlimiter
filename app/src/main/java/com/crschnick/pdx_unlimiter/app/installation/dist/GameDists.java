package com.crschnick.pdx_unlimiter.app.installation.dist;

import com.crschnick.pdx_unlimiter.app.installation.Game;
import com.crschnick.pdx_unlimiter.app.util.OsHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualLinkedHashBidiMap;
import org.apache.commons.lang3.SystemUtils;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

public class GameDists {

    private static final BidiMap<String, BiFunction<Game, Path, Optional<GameDist>>> COMPOUND_TYPES = new DualLinkedHashBidiMap<>();
    private static final BidiMap<String, BiFunction<Game, Path, Optional<GameDist>>> BASIC_DISTS = new DualLinkedHashBidiMap<>();
    private static final BidiMap<String, Class<? extends GameDist>> IDS = new DualLinkedHashBidiMap<>();
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

    static {
        COMPOUND_TYPES.put("windows-store", WindowsStoreDist::getDist);
        COMPOUND_TYPES.put("steam", SteamDist::getDist);

        BASIC_DISTS.put("pdx", PdxLauncherDist::getDist);
        BASIC_DISTS.put("legacy", LegacyLauncherDist::getDist);
        BASIC_DISTS.put("no-launcher", NoLauncherDist::getDist);
    }

    static {
        IDS.put("steam", SteamDist.class);
        IDS.put("windows-store", WindowsStoreDist.class);
        IDS.put("pdx", PdxLauncherDist.class);
        IDS.put("legacy", LegacyLauncherDist.class);
        IDS.put("no-launcher", NoLauncherDist.class);
    }

    public static Optional<GameDist> detectDist(Game g, Path dir) {
        return getCompoundDistFromDirectory(g, dir)
                .or(() -> GameDists.getBasicDistFromDirectory(g, dir));
    }

    public static Optional<GameDist> getBasicDistFromDirectory(Game g, Path dir) {
        for (var e : BASIC_DISTS.values()) {
            var r = e.apply(g, dir);
            if (r.isPresent()) {
                return r;
            }
        }
        return Optional.empty();
    }

    private static Optional<GameDist> getCompoundDistFromDirectory(Game g, Path dir) {
        for (var e : COMPOUND_TYPES.values()) {
            var r = e.apply(g, dir);
            if (r.isPresent()) {
                return r;
            }
        }
        return Optional.empty();
    }

    public static Optional<GameDist> getDist(Game g, JsonNode n) {
        if (n != null) {
            // Legacy support
            if (n.isTextual()) {
                return detectDist(g, Path.of(n.textValue()));
            }

            var type = n.get("type").textValue();
            var loc = Path.of(n.get("location").textValue());

            var exTypes = new HashMap<>(COMPOUND_TYPES);
            exTypes.putAll(BASIC_DISTS);
            return exTypes.get(type).apply(g, loc).or(() -> detectDist(g, null));
        } else {
            return detectDist(g, null);
        }
    }

    public static JsonNode toNode(GameDist d) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("type", IDS.inverseBidiMap().get(d.getClass()));
        node.put("location", d.getInstallLocation().toString());
        return node;
    }

    public static List<Path> getInstallDirSearchPaths() {
        return installDirSearchPaths;
    }
}

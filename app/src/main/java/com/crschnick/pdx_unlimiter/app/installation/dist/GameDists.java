package com.crschnick.pdx_unlimiter.app.installation.dist;

import com.crschnick.pdx_unlimiter.app.installation.Game;
import com.fasterxml.jackson.databind.JsonNode;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

public class GameDists {

    private static final Map<String, BiFunction<Game, Path, Optional<GameDist>>> TYPES = new LinkedHashMap<>();

    static {
        TYPES.put("steam", SteamDist::getDist);
        TYPES.put("windows-store", WindowsStoreDist::getDist);
        TYPES.put("pdx", PdxLauncherDist::getDist);
        TYPES.put("legacy", LegacyLauncherDist::getDist);
        TYPES.put("no-launcher", NoLauncherDist::getDist);
    }

    public static Optional<GameDist> getDist(Game g, JsonNode n) {
        // Legacy support
        if (n.isTextual()) {
            return getDistFromDirectory(g, Path.of(n.textValue()));
        }

        var type = n.get("type").textValue();
        var loc = Path.of(n.get("location").textValue());
        return TYPES.get(type).apply(g, loc).or(() -> getDistFromDirectory(g, null));
    }

    private static Optional<GameDist> getDistFromDirectory(Game g, Path dir) {
        for (var e : TYPES.values()) {
            var r = e.apply(g, dir);
            if (r.isPresent()) {
                return r;
            }
        }
        return Optional.empty();
    }
}

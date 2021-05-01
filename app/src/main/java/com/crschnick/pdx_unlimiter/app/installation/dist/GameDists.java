package com.crschnick.pdx_unlimiter.app.installation.dist;

import com.crschnick.pdx_unlimiter.app.installation.Game;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualLinkedHashBidiMap;

import java.nio.file.Path;
import java.util.Optional;
import java.util.function.BiFunction;

public class GameDists {

    private static final BidiMap<String, BiFunction<Game, Path, Optional<GameDist>>> TYPES = new DualLinkedHashBidiMap<>();
    private static final BidiMap<String, Class<? extends GameDist>> IDS = new DualLinkedHashBidiMap<>();


    static {
        TYPES.put("windows-store", WindowsStoreDist::getDist);
        TYPES.put("steam", SteamDist::getDist);
        TYPES.put("pdx", PdxLauncherDist::getDist);
        TYPES.put("legacy", LegacyLauncherDist::getDist);
        TYPES.put("no-launcher", NoLauncherDist::getDist);
    }

    static {
        IDS.put("steam", SteamDist.class);
        IDS.put("windows-store", WindowsStoreDist.class);
        IDS.put("pdx", PdxLauncherDist.class);
        IDS.put("legacy", LegacyLauncherDist.class);
        IDS.put("no-launcher", NoLauncherDist.class);
    }

    public static Optional<GameDist> getDist(Game g, JsonNode n) {
        if (n != null) {
            // Legacy support
            if (n.isTextual()) {
                return getDistFromDirectory(g, Path.of(n.textValue()));
            }

            var type = n.get("type").textValue();
            var loc = Path.of(n.get("location").textValue());
            return TYPES.get(type).apply(g, loc).or(() -> getDistFromDirectory(g, null));
        } else {
            return getDistFromDirectory(g, null);
        }
    }

    public static JsonNode toNode(GameDist d) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("type", IDS.inverseBidiMap().get(d.getClass()));
        node.put("location", d.getInstallLocation().toString());
        return node;
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

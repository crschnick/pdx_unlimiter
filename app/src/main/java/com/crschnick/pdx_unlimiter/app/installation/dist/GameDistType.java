package com.crschnick.pdx_unlimiter.app.installation.dist;

import com.crschnick.pdx_unlimiter.app.installation.Game;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiFunction;

public abstract class GameDistType {

    private static final Map<String, BiFunction<Game, JsonNode, Optional<GameDistType>>> TYPES = new LinkedHashMap<>();

    static {
        TYPES.put("windows-store", WindowsStoreDist::getDist);
        TYPES.put("steam", SteamDist::getDist);
    }

    public static Optional<GameDistType> getDist(Game g, JsonNode n) {
        var type = n.get("type").textValue();
        return TYPES.get(type).apply(g, n).or(() -> getDist(g));
    }

    public static Optional<GameDistType> getDist(Game g) {
        for (var e : TYPES.values()) {
            var r = e.apply(g, null);
            if (r.isPresent()) {
                return r;
            }
        }
        return Optional.empty();
    }

    private final Game game;
    private final String name;
    private final Path installLocation;

    public GameDistType(Game game, String name, Path installLocation) {
        this.game = game;
        this.name = name;
        this.installLocation = installLocation;
    }

    public void toNode(ObjectNode node) {
        node.put("location", getInstallLocation().toString());
    }

    public Path getInstallLocation() {
        return installLocation;
    }

    public abstract boolean supportsLauncher();

    public abstract boolean supportsDirectLaunch();

    public boolean directLaunch() {
        throw new UnsupportedOperationException();
    }

    public void startLauncher() throws IOException {
        throw new UnsupportedOperationException();
    }

    public List<Path> getAdditionalSavegamePaths() {
        return List.of();
    }

    public String getName() {
        return name;
    }

    public Game getGame() {
        return game;
    }
}

package com.crschnick.pdx_unlimiter.core.savegame;

import com.crschnick.pdx_unlimiter.core.format.NodeSplitter;
import com.crschnick.pdx_unlimiter.core.format.eu4.Eu4Transformer;
import com.crschnick.pdx_unlimiter.core.parser.Node;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

public class Eu4Savegame extends Savegame {

    public static final int VERSION = 14;

    private static final String[] SAVEGAME_PARTS = new String[]{"ai", "meta", "gamestate"};

    private static final String[] GAMESTATE_SPLIT_PARTS = new String[]{
            "active_wars", "previous_wars", "provinces", "countries", "countries_history",
            "trade_nodes", "rebel_factions", "active_advisors", "map_area_data", "religions", "diplomacy",
            "inflation_statistics", "religion_data"};

    private static final String[] INTERMEDIATE_PARTS = Stream
            .concat(Arrays.stream(SAVEGAME_PARTS), Arrays.stream(GAMESTATE_SPLIT_PARTS))
            .toArray(String[]::new);

    private Eu4Savegame(Map<String, Node> nodes, int version) {
        super(nodes, version);
    }

    public static Eu4Savegame fromSavegame(Eu4RawSavegame save) throws SavegameParseException {
        Node gameState = save.getGamestate();
        Map<String, Node> map;
        try {
            Eu4Transformer.GAMESTATE_TRANSFORMER.transform(gameState);
            Eu4Transformer.META_TRANSFORMER.transform(save.getMeta());
            map = new NodeSplitter(GAMESTATE_SPLIT_PARTS).removeNodes(gameState);
        } catch (RuntimeException e) {
            throw new SavegameParseException("Can't transform savegame", e);
        }
        map.put("gamestate", gameState);
        map.put("ai", save.getAi());
        map.put("meta", save.getMeta());
        return new Eu4Savegame(map, VERSION);
    }


    public static Eu4Savegame fromFile(Path file) throws IOException {
        return new Eu4Savegame(fromFile(file, VERSION, INTERMEDIATE_PARTS), VERSION);
    }
}

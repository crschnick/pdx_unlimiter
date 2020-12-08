package com.crschnick.pdx_unlimiter.core.savegame;

import com.crschnick.pdx_unlimiter.core.format.NodeSplitter;
import com.crschnick.pdx_unlimiter.core.format.stellaris.StellarisTransformer;
import com.crschnick.pdx_unlimiter.core.parser.Node;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

public class StellarisSavegame extends Savegame {

    public static final int VERSION = 1;

    private static final String[] SAVEGAME_PARTS = new String[]{"meta", "gamestate"};

    private static final String[] GAMESTATE_SPLIT_PARTS = new String[]{
            "nebulas", "countries", "fleets", "armies", "planets",
            "ships", "leaders", "wars", "species", "pops", "ambient_objects", "galactic_objects",
            "fleet_templates", "starbase_mgr", "ship_designs", "construction", "deposits", "megastructures", "buildings", "global_ship_designs"};

    private static final String[] INTERMEDIATE_PARTS = Stream
            .concat(Arrays.stream(SAVEGAME_PARTS), Arrays.stream(GAMESTATE_SPLIT_PARTS))
            .toArray(String[]::new);

    private StellarisSavegame(Map<String, Node> nodes, int version) {
        super(nodes, version);
    }

    public static StellarisSavegame fromSavegame(StellarisRawSavegame save) throws SavegameParseException {
        Node gameState = save.getGamestate();
        Map<String, Node> map;
        try {
            StellarisTransformer.TRANSFORMER.transform(gameState);
            StellarisTransformer.META_TRANSFORMER.transform(save.getMeta());
            map = new NodeSplitter(GAMESTATE_SPLIT_PARTS).removeNodes(gameState);
        } catch (RuntimeException e) {
            throw new SavegameParseException("Can't transform savegame", e);
        }
        map.put("gamestate", gameState);
        map.put("meta", save.getMeta());
        return new StellarisSavegame(map, VERSION);
    }

    public static StellarisSavegame fromFile(Path file) throws IOException {
        return new StellarisSavegame(fromFile(file, VERSION, INTERMEDIATE_PARTS), VERSION);
    }
}

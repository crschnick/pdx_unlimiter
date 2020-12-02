package com.crschnick.pdx_unlimiter.eu4.savegame;

import com.crschnick.pdx_unlimiter.eu4.format.NodeSplitter;
import com.crschnick.pdx_unlimiter.eu4.format.ck3.Ck3Transformer;
import com.crschnick.pdx_unlimiter.eu4.parser.Node;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class Ck3Savegame extends Savegame {


    public static final int VERSION = 2;

    private static final String[] PARTS = new String[]{"gamestate", "meta"};

    private static final String[] SPLIT_PARTS = new String[]{"provinces", "landed_titles", "armies", "ai",
            "triggered_events", "culture_manager", "wars", "religion", "faction_manager", "mercenary_company_manager",
            "succession", "sieges", "combats", "opinions", "relations", "units", "characters", "dynasties", "living",
            "important_action_manager", "character_lookup", "secrets", "county_manager", "council_task_manager",
            "coat_of_arms"};

    private static final String[] ALL_PARTS = Stream
            .concat(Arrays.stream(PARTS), Arrays.stream(SPLIT_PARTS))
            .toArray(String[]::new);

    private Ck3Savegame(Map<String, Node> nodes, int version) {
        super(nodes, version);
    }

    public static Ck3Savegame fromSavegame(Ck3RawSavegame save) throws SavegameParseException {
        Map<String, Node> map;
        try {
            Node gameState = save.getGamestate();
            Ck3Transformer.TRANSFORMER.transform(gameState);
            map = new HashMap<>(new NodeSplitter(SPLIT_PARTS).removeNodes(gameState));
            map.put("gamestate", gameState);
            Ck3Transformer.META_TRANSFORMER.transform(save.getMeta());
            map.put("meta", save.getMeta());
        } catch (Exception e) {
            throw new SavegameParseException("Can't transform savegame", e);
        }
        return new Ck3Savegame(map, VERSION);
    }

    public static Ck3Savegame fromFile(Path file) throws IOException {
        return new Ck3Savegame(fromFile(file, VERSION, ALL_PARTS), VERSION);
    }
}

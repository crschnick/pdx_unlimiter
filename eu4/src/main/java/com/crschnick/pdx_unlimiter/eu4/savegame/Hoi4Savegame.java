package com.crschnick.pdx_unlimiter.eu4.savegame;

import com.crschnick.pdx_unlimiter.eu4.format.NodeSplitter;
import com.crschnick.pdx_unlimiter.eu4.format.eu4.Eu4Transformer;
import com.crschnick.pdx_unlimiter.eu4.parser.Node;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public class Hoi4Savegame extends Savegame {

    public static final int VERSION = 12;

    private static final String[] PARTS = new String[]{"gamestate"};

    private Hoi4Savegame(Map<String, Node> nodes, int version) {
        super(nodes, version);
    }

    public static Hoi4Savegame fromSavegame(Hoi4RawSavegame save) throws SavegameParseException {
        Node gameState = save.getContent();
        Map<String, Node> map;
        try {
            map = Map.of("gamestate", gameState);
        } catch (Exception e) {
            throw new SavegameParseException("Can't transform savegame", e);
        }
        return new Hoi4Savegame(map, VERSION);
    }

    public static Hoi4Savegame fromFile(Path file) throws IOException {
        return new Hoi4Savegame(fromFile(file, VERSION, PARTS), VERSION);
    }
}

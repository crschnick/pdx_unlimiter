package com.paradox_challenges.eu4_unlimiter.format;

import com.paradox_challenges.eu4_unlimiter.parser.ArrayNode;
import com.paradox_challenges.eu4_unlimiter.parser.KeyValueNode;
import com.paradox_challenges.eu4_unlimiter.parser.Node;
import com.paradox_challenges.eu4_unlimiter.parser.ValueNode;
import com.paradox_challenges.eu4_unlimiter.parser.eu4.Eu4Savegame;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class NamespaceCreator {
    public static String createNamespace(Eu4Savegame unnamed, Eu4Savegame named) {
        String s = "";
        s += createNamespace(unnamed.getGamestate(), named.getGamestate());
        s += "\n";
        s += createNamespace(unnamed.getAi(), named.getAi());
        s += "\n";
        s += createNamespace(unnamed.getMeta(), named.getMeta());
        return s;
    }

    public static String createNamespace(Node unnamed, Node named) {
        StringBuilder b = new StringBuilder();
        Map<Integer,String> map = new HashMap<>();
        createNamespace(map, unnamed, named);
        for (var e : map.entrySet()) {
            b.append(e.getKey()).append("=").append(e.getValue()).append("\n");
        }
        return b.toString();
    }

    private static void createNamespace(Map<Integer,String> map, Node unnamed, Node named) {
        if (named instanceof ArrayNode && unnamed instanceof ArrayNode) {
            ArrayNode a = (ArrayNode) named;
            for (int i = 0; i < Math.min(a.getNodes().size(), ((ArrayNode) unnamed).getNodes().size()); i++) {
                createNamespace(map, ((ArrayNode) unnamed).getNodes().get(i), a.getNodes().get(i));
            }
        }

        if (named instanceof KeyValueNode && unnamed instanceof KeyValueNode) {
            KeyValueNode kv = (KeyValueNode) named;
            if (kv.getKeyName().equals(((KeyValueNode) unnamed).getKeyName())) {
                createNamespace(map, ((KeyValueNode) unnamed).getNode(), kv.getNode());
            }

            if (Pattern.compile("[0-9]+").matcher(kv.getKeyName()).matches()) {
                return;
            }

            if (Pattern.compile("[0-9]+").matcher(((KeyValueNode) unnamed).getKeyName()).matches()) {
                int i = Integer.parseInt(((KeyValueNode) unnamed).getKeyName());
                map.put(i, kv.getKeyName());
                createNamespace(map, ((KeyValueNode) unnamed).getNode(), ((KeyValueNode) named).getNode());
            }
        }
    }
}

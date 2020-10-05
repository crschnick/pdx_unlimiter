package com.paradox_challenges.eu4_unlimiter.format;

import com.paradox_challenges.eu4_unlimiter.parser.ArrayNode;
import com.paradox_challenges.eu4_unlimiter.parser.KeyValueNode;
import com.paradox_challenges.eu4_unlimiter.parser.Node;
import com.paradox_challenges.eu4_unlimiter.parser.ValueNode;
import com.paradox_challenges.eu4_unlimiter.parser.eu4.Eu4Savegame;

import java.util.*;
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
        Map<Integer,String> map = new LinkedHashMap<>();
        createNamespace(map, unnamed, named);
        for (var e : map.entrySet()) {
            b.append(e.getKey()).append("=").append(e.getValue()).append("\n");
        }
        return b.toString();
    }

    private static List<Node> compress(ArrayNode a) {
        Set<String> keys = new HashSet<>();
        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < a.getNodes().size(); i++) {
            Node n = a.getNodes().get(i);
            if (n instanceof KeyValueNode) {
                KeyValueNode kv = (KeyValueNode) n;
                if (keys.contains(kv.getKeyName())) {
                    //continue;
                } else {
                    keys.add(kv.getKeyName());
                }
                nodes.add(kv);
            }
        }
        return nodes;
    }

    private static void createNamespace(Map<Integer,String> map, Node unnamed, Node named) {
        if (named instanceof ArrayNode && unnamed instanceof ArrayNode) {
            ArrayNode a = (ArrayNode) named;
            ArrayNode ua = (ArrayNode) unnamed;
            var namedNodes = compress(a);
            var unnamedNodes = compress(ua);
            if (namedNodes.size() < 50 && namedNodes.size() != unnamedNodes.size()) {
                return;
            }
            for (int i = 0; i < Math.min(namedNodes.size(), 50); i++) {
                createNamespace(map, unnamedNodes.get(i), namedNodes.get(i));
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
                //System.err.println("Ambigous keys: " + map.get(i) + ", " + kv.getKeyName());
                //System.err.println("Ambigous values: " + ((KeyValueNode) unnamed).getNode() + ", " + kv.getNode().toString(0));
                if (!map.getOrDefault(i, kv.getKeyName()).equals(kv.getKeyName())) {
                    System.err.println("Ambigous keys: " + map.get(i) + ", " + kv.getKeyName());
                } else {

                    map.put(i, kv.getKeyName());
                }
            }
            createNamespace(map, ((KeyValueNode) unnamed).getNode(), ((KeyValueNode) named).getNode());
        }
    }
}

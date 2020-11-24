package com.crschnick.pdx_unlimiter.eu4.format;

import com.crschnick.pdx_unlimiter.eu4.parser.ArrayNode;
import com.crschnick.pdx_unlimiter.eu4.parser.KeyValueNode;
import com.crschnick.pdx_unlimiter.eu4.parser.Node;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class NamespaceCreator {
    public static String createNamespace(Map<String, Node> unnamed, Map<String, Node> named) {
        StringBuilder s = new StringBuilder();
        var set = unnamed.keySet();
        set.retainAll(named.keySet());
        for (String node : set) {
            s.append(createNamespace(unnamed.get(node), named.get(node)));
            s.append("\n");
        }
        return s.toString();
    }

    public static String createNamespace(Node unnamed, Node named) {
        StringBuilder b = new StringBuilder();
        Map<Integer, String> map = new LinkedHashMap<>();
        createNamespace(map, unnamed, named);
        for (var e : map.entrySet()) {
            b.append(e.getKey()).append("=").append(e.getValue()).append("\n");
        }
        return b.toString();
    }

    private static void createNamespace(Map<Integer, String> map, Node unnamed, Node named) {
        if (named instanceof ArrayNode && unnamed instanceof ArrayNode) {
            ArrayNode a = (ArrayNode) named;
            ArrayNode ua = (ArrayNode) unnamed;
            if (ua.getNodes().size() != a.getNodes().size()) {
                return;
            }
            for (int i = 0; i < a.getNodes().size(); i++) {
                createNamespace(map, ua.getNodes().get(i), a.getNodes().get(i));
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
                if (map.containsKey(i) && !map.get(i).equals(kv.getKeyName())) {
                    System.err.println("Ambigous keys: " + map.get(i) + ", " + kv.getKeyName());
                } else {
                    map.put(i, kv.getKeyName());
                }
            }
            createNamespace(map, ((KeyValueNode) unnamed).getNode(), ((KeyValueNode) named).getNode());
        }
    }
}

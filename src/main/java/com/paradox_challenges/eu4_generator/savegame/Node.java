package com.paradox_challenges.eu4_generator.savegame;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class Node {

    public static String indent(int amount) {
        String s = "";
        for (int i = 0; i < amount; i++) {
            s = s.concat("  ");
        }
        return s;
    }

    public static Optional<Node> getNodeForKeyIfExistent(Node node, String key) {
        var list = getNodesForKey(node, key);
        return list.size() == 0 ? Optional.empty() : Optional.of(list.get(0));
    }

    public static Node getNodeForKey(Node node, String key) {
        var list = getNodesForKey(node, key);
        if (list.size() > 1) {
            throw new IllegalArgumentException("Invalid key: " + key);
        }
        return list.get(0);
    }

    public static KeyValueNode getKeyValueNodeForKey(Node node, String key) {
        var list = getKeyValueNodesForKey(node, key);
        if (list.size() > 1) {
            throw new IllegalArgumentException("Invalid key: " + key);
        }
        return list.get(0);
    }

    public static List<KeyValueNode> getKeyValueNodesForKey(Node node, String key) {
        List<KeyValueNode> nodes = new ArrayList<>();
        if (node instanceof ArrayNode) {
            ArrayNode array = (ArrayNode) node;
            for (Node sub : array.getNodes()) {
                if (sub instanceof KeyValueNode) {
                    KeyValueNode kvNode = (KeyValueNode) sub;
                    if (kvNode.getKeyName().equals(key)) {
                        nodes.add(kvNode);
                    }
                }
            }
        }
        return nodes;
    }

    public static List<Node> getNodesForKey(Node node, String key) {
        List<Node> nodes = new ArrayList<>();
        if (node instanceof ArrayNode) {
            ArrayNode array = (ArrayNode) node;
            for (Node sub : array.getNodes()) {
                if (sub instanceof KeyValueNode) {
                    KeyValueNode kvNode = (KeyValueNode) sub;
                    if (kvNode.getKeyName().equals(key)) {
                        nodes.add(kvNode.getNode());
                    }
                }
            }
        }
        return nodes;
    }

    public abstract String toString(int indentation);
}

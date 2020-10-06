package com.paradox_challenges.eu4_unlimiter.parser;

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

    public static List<Node> copyOfArrayNode(Node node) {
        ArrayNode a = (ArrayNode) node;
        return new ArrayList<>(a.getNodes());
    }

    public static void addNodeToArray(Node node, Node toAdd) {
        ArrayNode a = (ArrayNode) node;
        a.addNode(toAdd);
    }

    public static void removeNodeFromArray(Node node, Node toRemove) {
        ArrayNode a = (ArrayNode) node;
        a.removeNode(toRemove);
    }

    public static List<Node> getNodesForKeys(Node node, String[] keys) {
        List<Node> nodes = List.of(node);
        List<Node> newNodes = new ArrayList<>();
        for (String s : keys) {
            for (Node current : nodes) {
                newNodes.addAll(Node.getKeyValueNodesForKey(current, s));
            }
            nodes = newNodes;
        }
        return nodes;
    }

    public static Optional<Node> getNodeForKeyIfExistent(Node node, String key) {
        var list = getNodesForKey(node, key);
        return list.size() == 0 ? Optional.empty() : Optional.of(list.get(0));
    }

    public static String getString(Node node) {
        return ((ValueNode<String>) node).getValue();
    }

    public static int getInteger(Node node) {
        if (((ValueNode) node).getValue() instanceof Long) {
            long v = (long) ((ValueNode) node).getValue();
            if (v >= Integer.MIN_VALUE && v <= Integer.MAX_VALUE) {
                return (int) v;
            }
        }
        return ((ValueNode<Integer>) node).getValue();
    }



    public static List<Node> getNodeArray(Node node) {
        return ((ArrayNode) node).getNodes();
    }

    public static boolean hasKey(Node node, String key) {
        if (!(node instanceof ArrayNode)) {
            return false;
        }

        var list = getNodesForKey(node, key);
        return list.size() > 0;
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
        if (list.size() == 0) {
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
                    if (key.equals("*") || kvNode.getKeyName().equals(key)) {
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
                    if (key.equals("*") || kvNode.getKeyName().equals(key)) {
                        nodes.add(kvNode.getNode());
                    }
                }
            }
        }
        return nodes;
    }

    public abstract String toString(int indentation);
}

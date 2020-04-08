package com.paradox_challenges.eu4_unlimiter.converter;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.paradox_challenges.eu4_unlimiter.parser.ArrayNode;
import com.paradox_challenges.eu4_unlimiter.parser.KeyValueNode;
import com.paradox_challenges.eu4_unlimiter.parser.Node;
import com.paradox_challenges.eu4_unlimiter.parser.ValueNode;

public class JsonConverter {

    private static boolean getFlatValue(ObjectNode jsonNode, String key, Node node) {
        if (node instanceof ValueNode) {
            Object value = ((ValueNode) node).getValue();
            if (value instanceof Boolean) {
                jsonNode.put(key, (boolean) value);
            }
            if (value instanceof Integer) {
                jsonNode.put(key, (int) value);
            }
            if (value instanceof String) {
                jsonNode.put(key, (String) value);
            }
            if (value instanceof Float) {
                jsonNode.put(key, (Float) value);
            }
            return true;
        } else if (node instanceof ArrayNode) {
            ArrayNode a = (ArrayNode) node;
            if (a.getNodes().size() == 0) {
                return false;
            }

            if (a.getNodes().get(0) instanceof ValueNode) {
                com.fasterxml.jackson.databind.node.ArrayNode array = jsonNode.putArray(key);
                for (Node n : a.getNodes()) {
                    Object value = ((ValueNode) n).getValue();
                    if (value instanceof Boolean) {
                        array.add((boolean) value);
                    }
                    if (value instanceof Integer) {
                        array.add((Integer) value);
                    }
                    if (value instanceof String) {
                        array.add((String) value);
                    }
                    if (value instanceof Float) {
                        array.add((Float) value);
                    }
                }
                return true;
            } else if (a.getNodes().get(0) instanceof ArrayNode) {
                com.fasterxml.jackson.databind.node.ArrayNode array = jsonNode.putArray(key);
                for (Node n : a.getNodes()) {
                    toJsonObject(array.addObject(), n);
                }
                return true;
            } else {
                return false;
            }
        }
        throw new IllegalArgumentException();
    }

    public static void toJsonObject(ObjectNode jsonNode, Node node) {
        if (node instanceof ArrayNode) {
            ArrayNode a = (ArrayNode) node;
            for (Node sub : a.getNodes()) {
                if (sub instanceof KeyValueNode) {
                    String key = ((KeyValueNode) sub).getKeyName();
                    boolean isFlat = getFlatValue(jsonNode, key, ((KeyValueNode) sub).getNode());
                    if (!isFlat) {
                        toJsonObject(jsonNode.putObject(key), ((KeyValueNode) sub).getNode());
                    }
                }
            }
        }
    }
}

package com.crschnick.pdx_unlimiter.eu4.io;

import com.crschnick.pdx_unlimiter.eu4.parser.KeyValueNode;
import com.crschnick.pdx_unlimiter.eu4.parser.Node;
import com.crschnick.pdx_unlimiter.eu4.parser.ValueNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.crschnick.pdx_unlimiter.eu4.parser.ArrayNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class JsonConverter {

    private static boolean getFlatValue(ObjectNode jsonNode, String key, Node node) {
        if (node instanceof ValueNode) {
            Object value = ((ValueNode) node).getValue();
            if (value instanceof Boolean) {
                jsonNode.put(key, (boolean) value);
            }
            if (value instanceof Long) {
                jsonNode.put(key, (long) value);
            }
            if (value instanceof String) {
                jsonNode.put(key, (String) value);
            }
            if (value instanceof Double) {
                jsonNode.put(key, (Double) value);
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
                    if (value instanceof Long) {
                        array.add((Long) value);
                    }
                    if (value instanceof String) {
                        array.add((String) value);
                    }
                    if (value instanceof Double) {
                        array.add((Double) value);
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

    public static Node fromJson(JsonNode jsonNode) {
        if (jsonNode.isArray()) {
            com.fasterxml.jackson.databind.node.ArrayNode a = (com.fasterxml.jackson.databind.node.ArrayNode) jsonNode;
            List<Node> nodeList = new ArrayList<>();
            for (int i = 0; i < a.size(); i++) {
                nodeList.add(fromJson(a.get(i)));
            }
            return new ArrayNode(nodeList);
        }

        if (jsonNode.isObject()) {
            List<Node> nodeList = new ArrayList<>();
            ObjectNode obj = (ObjectNode) jsonNode;
            for (Iterator<Map.Entry<String, JsonNode>> it = obj.fields(); it.hasNext(); ) {
                Map.Entry<String, JsonNode> entry = it.next();
                nodeList.add(new KeyValueNode(entry.getKey(), fromJson(entry.getValue())));
            }
            return new ArrayNode(nodeList);
        }

        if (jsonNode.isBoolean()) return new ValueNode<Object>(jsonNode.booleanValue());
        if (jsonNode.isFloat() || jsonNode.isDouble()) return new ValueNode<Object>(jsonNode.doubleValue());
        if (jsonNode.isInt() || jsonNode.isLong()) return new ValueNode<Object>(jsonNode.longValue());
        if (jsonNode.isTextual()) return new ValueNode<Object>(jsonNode.textValue());
        throw new RuntimeException("");
    }
}

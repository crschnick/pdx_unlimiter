package com.crschnick.pdx_unlimiter.core.io;

import com.crschnick.pdx_unlimiter.core.parser.ArrayNode;
import com.crschnick.pdx_unlimiter.core.parser.KeyValueNode;
import com.crschnick.pdx_unlimiter.core.parser.Node;
import com.crschnick.pdx_unlimiter.core.parser.ValueNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class JsonConverter {

    private static JsonNode valueToNode(ValueNode node) {
        Object value = node.getValue();
        if (value instanceof Boolean) {
            boolean b = (boolean) value;
            return JsonNodeFactory.instance.booleanNode(b);
        } else if (value instanceof Long) {
            long l = (long) value;
            return JsonNodeFactory.instance.numberNode(l);
        } else if (value instanceof String) {
            String s = (String) value;
            return JsonNodeFactory.instance.textNode(s);
        } else if (value instanceof Double) {
            double d = (double) value;
            return JsonNodeFactory.instance.numberNode(d);
        } else {
            throw new IllegalArgumentException();
        }
    }

    public static JsonNode toJsonObject(Node node) {
        if (node instanceof ValueNode) {
            return valueToNode((ValueNode) node);
        } else if (node instanceof ArrayNode) {
            ArrayNode a = (ArrayNode) node;
            if (a.getNodes().size() == 0) {
                return JsonNodeFactory.instance.arrayNode();
            }

            boolean isValueArray = a.getNodes().get(0) instanceof ValueNode;
            boolean isObject = a.getNodes().get(0) instanceof KeyValueNode;
            if (isValueArray) {
                com.fasterxml.jackson.databind.node.ArrayNode array = JsonNodeFactory.instance.arrayNode();
                for (Node n : a.getNodes()) {
                    array.add(valueToNode((ValueNode) n));
                }
                return array;
            } else if (isObject) {
                ObjectNode object = JsonNodeFactory.instance.objectNode();
                for (Node n : a.getNodes()) {
                    if (!(n instanceof KeyValueNode)) {
                        //TODO
                        int b = 0;
                    }
                    KeyValueNode kv = (KeyValueNode) n;
                    object.set(kv.getKeyName(), toJsonObject(kv.getNode()));
                }
                return object;
            } else {
                com.fasterxml.jackson.databind.node.ArrayNode array = JsonNodeFactory.instance.arrayNode();
                for (Node n : a.getNodes()) {
                    array.add(toJsonObject(n));
                }
                return array;
            }
        } else {
            throw new RuntimeException("");
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

        if (jsonNode.isBoolean()) return new ValueNode(jsonNode.booleanValue());
        if (jsonNode.isFloat() || jsonNode.isDouble()) return new ValueNode(jsonNode.doubleValue());
        if (jsonNode.isInt() || jsonNode.isLong()) return new ValueNode(jsonNode.longValue());
        if (jsonNode.isTextual()) return new ValueNode(jsonNode.textValue());
        throw new RuntimeException("");
    }
}

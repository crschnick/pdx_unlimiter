package com.paradox_challenges.eu4_unlimiter.converter;

import com.paradox_challenges.eu4_unlimiter.parser.ArrayNode;
import com.paradox_challenges.eu4_unlimiter.parser.KeyValueNode;
import com.paradox_challenges.eu4_unlimiter.parser.Node;
import com.paradox_challenges.eu4_unlimiter.parser.ValueNode;

import java.util.*;

public class JsonConverter {

    private static Optional<Object> getFlatValue(Node node) {
        if (node instanceof ValueNode) {
            return Optional.of(node);
        } else if (node instanceof ArrayNode) {
            ArrayNode a = (ArrayNode) node;
            if (a.getNodes().size() == 0) {
                return Optional.empty();
            }

            if (a.getNodes().get(0) instanceof ValueNode) {
                List<Object> values = new ArrayList<>();
                for (Node n : a.getNodes()) {
                    values.add(((ValueNode) n).getValue());
                }
                return Optional.of(values);
            } else {
                return Optional.empty();
            }
        }
        throw new IllegalArgumentException();
    }

    public static Map<String,Object> toJsonObject(Node node) {
        Map<String,Object> map = new HashMap<>();
        if (node instanceof ArrayNode) {
            ArrayNode a = (ArrayNode) node;
            for (Node sub : a.getNodes()) {
                if (sub instanceof KeyValueNode) {
                    String key = ((KeyValueNode) sub).getKeyName();
                    Optional<Object> toPut = getFlatValue(((KeyValueNode) sub).getNode());
                    if (toPut.isPresent()) {
                        map.put(key, toPut.get());
                    } else {
                        map.put(key, toJsonObject(((KeyValueNode) sub).getNode()));
                    }
                }
            }
        }
        return map;
    }
}

package com.crschnick.pdx_unlimiter.eu4.format;

import com.crschnick.pdx_unlimiter.eu4.parser.ArrayNode;
import com.crschnick.pdx_unlimiter.eu4.parser.KeyValueNode;
import com.crschnick.pdx_unlimiter.eu4.parser.Node;
import com.crschnick.pdx_unlimiter.eu4.parser.ValueNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ArrayNameTransformer extends NodeTransformer {

    protected Map<Integer,String> names;

    private static final String UNKNOWN = "unknown_";

    public ArrayNameTransformer(Map<Integer, String> names) {
        this.names = names;
    }

    @Override
    public void transform(Node node) {
        ArrayNode arrayNode = (ArrayNode) node;
        int counter = 0;
        for (Node sub : new ArrayList<>(arrayNode.getNodes())) {
            if (sub instanceof ValueNode) {
                arrayNode.getNodes().set(counter, KeyValueNode.create(names.getOrDefault(counter, UNKNOWN + String.valueOf(counter)), sub));
            }
            counter++;
        }
    }

    @Override
    public void reverse(Node node) {

    }
}

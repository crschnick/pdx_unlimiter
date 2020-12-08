package com.crschnick.pdx_unlimiter.core.format;

import com.crschnick.pdx_unlimiter.core.parser.ArrayNode;
import com.crschnick.pdx_unlimiter.core.parser.KeyValueNode;
import com.crschnick.pdx_unlimiter.core.parser.Node;
import com.crschnick.pdx_unlimiter.core.parser.ValueNode;

import java.util.ArrayList;
import java.util.Map;

public class ArrayNameTransformer extends NodeTransformer {

    private static final String UNKNOWN = "unknown_";
    protected Map<Integer, String> names;

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

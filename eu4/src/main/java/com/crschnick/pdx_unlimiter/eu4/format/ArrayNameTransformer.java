package com.crschnick.pdx_unlimiter.eu4.format;

import com.crschnick.pdx_unlimiter.eu4.parser.ArrayNode;
import com.crschnick.pdx_unlimiter.eu4.parser.KeyValueNode;
import com.crschnick.pdx_unlimiter.eu4.parser.Node;

import java.util.ArrayList;

public class ArrayNameTransformer extends NodeTransformer {

    private String[] names;

    public ArrayNameTransformer(String[] names) {
        this.names = names;
    }

    @Override
    public void transform(Node node) {

        ArrayNode arrayNode = (ArrayNode) node;
        int counter = 0;
        for (Node sub : new ArrayList<>(arrayNode.getNodes())) {
            if (!(sub instanceof KeyValueNode)) {
                arrayNode.getNodes().remove(sub);
                arrayNode.addNode(KeyValueNode.create(names[counter], sub));
            }
            counter++;
        }
    }

    @Override
    public void reverse(Node node) {

    }
}

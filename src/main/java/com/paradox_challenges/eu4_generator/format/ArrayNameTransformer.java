package com.paradox_challenges.eu4_generator.format;

import com.paradox_challenges.eu4_generator.parser.ArrayNode;
import com.paradox_challenges.eu4_generator.parser.KeyValueNode;
import com.paradox_challenges.eu4_generator.parser.Node;

import java.util.ArrayList;

public class ArrayNameTransformer extends NodeTransformer {

    private String[] names;

    public ArrayNameTransformer(String[] names) {
        this.names = names;
    }

    @Override
    public Node transformNode(Node node) {
        ArrayNode arrayNode = (ArrayNode) node;
        int counter = 0;
        for (Node sub : new ArrayList<>(arrayNode.getNodes())) {
            if (!(sub instanceof KeyValueNode)) {
                arrayNode.getNodes().remove(sub);
                arrayNode.addNode(KeyValueNode.create(names[counter], sub));
            }
            counter++;
        }
        return arrayNode;
    }
}

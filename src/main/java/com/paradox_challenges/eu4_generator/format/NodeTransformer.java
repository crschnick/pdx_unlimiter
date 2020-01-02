package com.paradox_challenges.eu4_generator.format;

import com.paradox_challenges.eu4_generator.savegame.ArrayNode;
import com.paradox_challenges.eu4_generator.savegame.KeyValueNode;
import com.paradox_challenges.eu4_generator.savegame.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class NodeTransformer {


    protected Optional<Node> getNodeForKeyIfExistant(Node node, String key) {
        var list = getNodesForKey(node, key);
        return list.size() == 0 ? Optional.empty() : Optional.of(list.get(0));
    }

    protected Node getNodeForKey(Node node, String key) {
        var list = getNodesForKey(node, key);
        if (list.size() > 1) {
            throw new IllegalArgumentException("Invalid key: " + key);
        }
        return getNodesForKey(node, key).get(0);
    }

    protected List<Node> getNodesForKey(Node node, String key) {
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

    public abstract Node transformNode(Node node);

}

package com.paradox_challenges.eu4_unlimiter.format;

import com.paradox_challenges.eu4_unlimiter.parser.ArrayNode;
import com.paradox_challenges.eu4_unlimiter.parser.KeyValueNode;
import com.paradox_challenges.eu4_unlimiter.parser.Node;

import java.util.ArrayList;
import java.util.List;

public class CollectNodesTransformer extends NodeTransformer {

    private String originalKeyName;

    private String newKeyName;

    public CollectNodesTransformer(String originalKeyName, String newKeyName) {
        this.originalKeyName = originalKeyName;
        this.newKeyName = newKeyName;
    }

    @Override
    public Node transformNode(Node node) {
        ArrayNode masterNode = (ArrayNode) node;
        List<KeyValueNode> warNodes = Node.getKeyValueNodesForKey(node, originalKeyName);
        List<Node> newWarNodes = new ArrayList<>();
        for (KeyValueNode warNode : warNodes) {
            masterNode.removeNode(warNode);
            newWarNodes.add(warNode.getNode());
        }
        masterNode.addNode(new KeyValueNode(newKeyName, new ArrayNode(newWarNodes)));
        return masterNode;
    }
}

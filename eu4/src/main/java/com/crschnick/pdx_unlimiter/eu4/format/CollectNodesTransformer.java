package com.crschnick.pdx_unlimiter.eu4.format;

import com.crschnick.pdx_unlimiter.eu4.parser.ArrayNode;
import com.crschnick.pdx_unlimiter.eu4.parser.KeyValueNode;
import com.crschnick.pdx_unlimiter.eu4.parser.Node;

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
    public void transform(Node node) {

        ArrayNode masterNode = (ArrayNode) node;
        List<KeyValueNode> warNodes = Node.getKeyValueNodesForKey(node, originalKeyName);
        List<Node> newWarNodes = new ArrayList<>();
        for (KeyValueNode warNode : warNodes) {
            masterNode.removeNode(warNode);
            newWarNodes.add(warNode.getNode());
        }
        masterNode.addNode(new KeyValueNode(newKeyName, new ArrayNode(newWarNodes)));
    }

    @Override
    public void reverse(Node node) {

    }
}

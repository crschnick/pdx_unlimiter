package com.paradox_challenges.eu4_unlimiter.format;

import com.paradox_challenges.eu4_unlimiter.parser.ArrayNode;
import com.paradox_challenges.eu4_unlimiter.parser.KeyValueNode;
import com.paradox_challenges.eu4_unlimiter.parser.Node;

public class RenameKeyTransformer extends NodeTransformer {

    private String oldName;

    private String newName;

    public RenameKeyTransformer(String oldName, String newName) {
        this.oldName = oldName;
        this.newName = newName;
    }

    @Override
    public Node transformNode(Node node) {
        KeyValueNode kv = (KeyValueNode) Node.getKeyValueNodeForKey(node, oldName);
        ArrayNode arrayNode = (ArrayNode) node;
        arrayNode.removeNode(kv);
        arrayNode.addNode(KeyValueNode.create(newName, kv.getNode()));
        return null;
    }
}

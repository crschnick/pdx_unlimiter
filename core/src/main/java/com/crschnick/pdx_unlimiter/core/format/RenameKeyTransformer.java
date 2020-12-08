package com.crschnick.pdx_unlimiter.core.format;

import com.crschnick.pdx_unlimiter.core.parser.ArrayNode;
import com.crschnick.pdx_unlimiter.core.parser.KeyValueNode;
import com.crschnick.pdx_unlimiter.core.parser.Node;

public class RenameKeyTransformer extends NodeTransformer {

    private String oldName;

    private String newName;

    public RenameKeyTransformer(String oldName, String newName) {
        this.oldName = oldName;
        this.newName = newName;
    }

    @Override
    public void transform(Node node) {
        KeyValueNode kv = Node.getKeyValueNodeForKey(node, oldName);
        ArrayNode arrayNode = (ArrayNode) node;
        arrayNode.removeNode(kv);
        arrayNode.addNode(KeyValueNode.create(newName, kv.getNode()));
    }

    @Override
    public void reverse(Node node) {
        KeyValueNode kv = Node.getKeyValueNodeForKey(node, newName);
        ArrayNode arrayNode = (ArrayNode) node;
        arrayNode.removeNode(kv);
        arrayNode.addNode(KeyValueNode.create(oldName, kv.getNode()));
    }


}

package com.crschnick.pdx_unlimiter.core.format;

import com.crschnick.pdx_unlimiter.core.parser.KeyValueNode;
import com.crschnick.pdx_unlimiter.core.parser.Node;

public class DefaultValueTransformer extends NodeTransformer {

    private String key;
    private Node defaultNode;

    public DefaultValueTransformer(String key, Node defaultNode) {
        this.key = key;
        this.defaultNode = defaultNode;
    }

    @Override
    public void transform(Node node) {
        if (!Node.getNodeForKeyIfExistent(node, key).isPresent()) {
            Node.addNodeToArray(node, KeyValueNode.create(key, defaultNode));
        }
    }

    @Override
    public void reverse(Node node) {

    }
}

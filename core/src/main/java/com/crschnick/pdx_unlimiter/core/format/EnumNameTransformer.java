package com.crschnick.pdx_unlimiter.core.format;

import com.crschnick.pdx_unlimiter.core.parser.KeyValueNode;
import com.crschnick.pdx_unlimiter.core.parser.Node;
import com.crschnick.pdx_unlimiter.core.parser.ValueNode;

import java.util.Map;

public class EnumNameTransformer extends NodeTransformer {

    private Map<Integer, String> names;

    public EnumNameTransformer(Map<Integer, String> names) {
        this.names = names;
    }

    @Override
    public void transform(Node node) {
        KeyValueNode kv = Node.getKeyValueNode(node);
        if (!names.containsKey(Node.getInteger(kv.getNode()))) {
            throw new IllegalArgumentException("");
        }

        kv.setNode(new ValueNode(names.get(Node.getInteger(kv.getNode()))));
    }

    @Override
    public void reverse(Node node) {

    }
}

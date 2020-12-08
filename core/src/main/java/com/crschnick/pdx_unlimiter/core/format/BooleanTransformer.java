package com.crschnick.pdx_unlimiter.core.format;

import com.crschnick.pdx_unlimiter.core.parser.KeyValueNode;
import com.crschnick.pdx_unlimiter.core.parser.Node;
import com.crschnick.pdx_unlimiter.core.parser.ValueNode;

public class BooleanTransformer extends NodeTransformer {

    public static NodeTransformer recursive() {
        return new RecursiveTransformer((n) -> {
            if (n instanceof KeyValueNode) {
                Node val = Node.getKeyValueNode(n).getNode();
                return val instanceof ValueNode &&
                        (((ValueNode) val).getValue() instanceof String && (Node.getString(val).equals("yes") || Node.getString(val).equals("no")));
            } else {
                return false;
            }
        }, new BooleanTransformer());
    }

    @Override
    public void transform(Node node) {
        KeyValueNode kv = (KeyValueNode) node;
        ValueNode v = (ValueNode) kv.getNode();
        kv.setNode(new ValueNode(v.getValue().equals("yes")));
    }

    @Override
    public void reverse(Node node) {

    }
}

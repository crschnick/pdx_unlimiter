package com.crschnick.pdx_unlimiter.eu4.format;

import com.crschnick.pdx_unlimiter.eu4.parser.KeyValueNode;
import com.crschnick.pdx_unlimiter.eu4.parser.Node;
import com.crschnick.pdx_unlimiter.eu4.parser.ValueNode;

public class BooleanTransformer extends NodeTransformer {

    @Override
    public void transform(Node node) {
        KeyValueNode kv = (KeyValueNode) node;
        ValueNode<String> v = (ValueNode<String>) kv.getNode();
        kv.setNode(new ValueNode<Boolean>(v.getValue().equals("yes")));
    }

    @Override
    public void reverse(Node node) {

    }
}

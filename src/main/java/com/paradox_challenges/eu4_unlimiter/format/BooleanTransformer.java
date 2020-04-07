package com.paradox_challenges.eu4_unlimiter.format;

import com.paradox_challenges.eu4_unlimiter.parser.GameDate;
import com.paradox_challenges.eu4_unlimiter.parser.KeyValueNode;
import com.paradox_challenges.eu4_unlimiter.parser.Node;
import com.paradox_challenges.eu4_unlimiter.parser.ValueNode;

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

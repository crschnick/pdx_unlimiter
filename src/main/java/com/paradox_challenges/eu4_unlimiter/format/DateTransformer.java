package com.paradox_challenges.eu4_unlimiter.format;

import com.paradox_challenges.eu4_unlimiter.parser.*;

import java.time.Month;
import java.util.ArrayList;
import java.util.List;

public class DateTransformer extends NodeTransformer {

    @Override
    public void transform(Node node) {
        KeyValueNode kv = (KeyValueNode) node;
        ValueNode<Object> v = (ValueNode<Object>) kv.getNode();
        GameDate d = null;
        if (v.getValue() instanceof Long) {
            d = GameDate.fromLong((Long) v.getValue());
        }
        if (v.getValue() instanceof String) {
            String s = (String) v.getValue();
            d = GameDate.fromString(s);
        }
        kv.setNode(GameDate.toNode(d));
    }

    @Override
    public void reverse(Node node) {
        KeyValueNode kv = (KeyValueNode) node;
        kv.setNode(new ValueNode<String>(GameDate.fromNode(kv.getNode()).toString()));
    }
}

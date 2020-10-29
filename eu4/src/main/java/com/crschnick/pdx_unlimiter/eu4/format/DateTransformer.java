package com.crschnick.pdx_unlimiter.eu4.format;

import com.crschnick.pdx_unlimiter.eu4.parser.GameDate;
import com.crschnick.pdx_unlimiter.eu4.parser.KeyValueNode;
import com.crschnick.pdx_unlimiter.eu4.parser.Node;
import com.crschnick.pdx_unlimiter.eu4.parser.ValueNode;

public class DateTransformer extends NodeTransformer {

    @Override
    public void transform(Node node) {
        KeyValueNode kv = (KeyValueNode) node;
        ValueNode v = (ValueNode) kv.getNode();
        GameDate d = null;
        if (v.getValue() instanceof Long) {
            d = GameDate.fromLong((Long) v.getValue());
        } else if (v.getValue() instanceof String) {
            String s = (String) v.getValue();
            d = GameDate.fromString(s);
        }
        if (d == null) {
            throw new IllegalArgumentException("Invalid date value " + v.getValue());
        }
        kv.setNode(GameDate.toNode(d));
    }

    @Override
    public void reverse(Node node) {
        KeyValueNode kv = (KeyValueNode) node;
        kv.setNode(new ValueNode(GameDate.fromNode(kv.getNode()).toString()));
    }
}

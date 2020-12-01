package com.crschnick.pdx_unlimiter.eu4.format;

import com.crschnick.pdx_unlimiter.eu4.data.GameDate;
import com.crschnick.pdx_unlimiter.eu4.data.GameDateType;
import com.crschnick.pdx_unlimiter.eu4.parser.KeyValueNode;
import com.crschnick.pdx_unlimiter.eu4.parser.Node;
import com.crschnick.pdx_unlimiter.eu4.parser.ValueNode;

import java.util.function.Function;
import java.util.function.Predicate;

public class DateTransformer extends NodeTransformer {

    public static final DateTransformer EU4 = new DateTransformer(GameDateType.EU4);
    public static final DateTransformer CK3 = new DateTransformer(GameDateType.CK3);
    public static final DateTransformer HOI4 = new DateTransformer(GameDateType.HOI4);
    public static final DateTransformer STELLARIS = new DateTransformer(GameDateType.STELLARIS);

    private GameDateType type;

    public static NodeTransformer recursive(DateTransformer t, Predicate<String> isDateEntry) {
        return new RecursiveTransformer((n) -> {
            if (n instanceof KeyValueNode && isDateEntry.test(Node.getKeyValueNode(n).getKeyName())) {
                Node val = Node.getKeyValueNode(n).getNode();
                return val instanceof ValueNode &&
                        (((ValueNode) val).getValue() instanceof String || ((ValueNode) val).getValue() instanceof Long);
            } else {
                return false;
            }
        }, t);
    }

    public DateTransformer(GameDateType type) {
        this.type = type;
    }

    @Override
    public void transform(Node node) {
        KeyValueNode kv = (KeyValueNode) node;
        ValueNode v = (ValueNode) kv.getNode();
        GameDate d = null;
        if (v.getValue() instanceof Long) {
            d = type.fromLong((Long) v.getValue());
        } else if (v.getValue() instanceof String) {
            String s = (String) v.getValue();
            d = type.fromString(s);
        }
        kv.setNode(type.toNode(d));
    }

    @Override
    public void reverse(Node node) {
        KeyValueNode kv = (KeyValueNode) node;
        kv.setNode(new ValueNode(type.fromNode(kv.getNode()).toString()));
    }
}

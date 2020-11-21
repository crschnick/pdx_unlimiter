package com.crschnick.pdx_unlimiter.eu4.format.hoi4;

import com.crschnick.pdx_unlimiter.eu4.format.*;
import com.crschnick.pdx_unlimiter.eu4.format.eu4.EventTransformer;
import com.crschnick.pdx_unlimiter.eu4.format.eu4.ProvincesTransformer;
import com.crschnick.pdx_unlimiter.eu4.parser.KeyValueNode;
import com.crschnick.pdx_unlimiter.eu4.parser.Node;
import com.crschnick.pdx_unlimiter.eu4.parser.ValueNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Hoi4Transformer {

    public static final NodeTransformer TRANSFORMER = createHoi4Transformer();

    private static boolean isDateEntry(String key) {
        return key.endsWith("date");
    }

    private static NodeTransformer createHoi4Transformer() {
        List<NodeTransformer> t = new ArrayList<>();
        t.add(new RecursiveTransformer((n) -> {
            if (n instanceof KeyValueNode && isDateEntry(Node.getKeyValueNode(n).getKeyName())) {
                Node val = Node.getKeyValueNode(n).getNode();
                return val instanceof ValueNode &&
                        (((ValueNode) val).getValue() instanceof String || ((ValueNode) val).getValue() instanceof Long);
            } else {
                return false;
            }
        }, DateTransformer.HOI4));

        t.add(new RecursiveTransformer((n) -> {
            if (n instanceof KeyValueNode) {
                Node val = Node.getKeyValueNode(n).getNode();
                return val instanceof ValueNode &&
                        (((ValueNode) val).getValue() instanceof String && (Node.getString(val).equals("yes") || Node.getString(val).equals("no")));
            } else {
                return false;
            }
        }, new BooleanTransformer()));

        t.add(new SubnodeTransformer(Map.of(new String[]{"countries", "*"}, new CountryTransformer()), true));

        return new ChainTransformer(t);
    }
}

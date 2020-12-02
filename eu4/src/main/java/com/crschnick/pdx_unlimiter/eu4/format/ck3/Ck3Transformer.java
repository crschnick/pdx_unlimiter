package com.crschnick.pdx_unlimiter.eu4.format.ck3;

import com.crschnick.pdx_unlimiter.eu4.format.*;
import com.crschnick.pdx_unlimiter.eu4.format.hoi4.CountryTransformer;
import com.crschnick.pdx_unlimiter.eu4.parser.ArrayNode;
import com.crschnick.pdx_unlimiter.eu4.parser.KeyValueNode;
import com.crschnick.pdx_unlimiter.eu4.parser.Node;
import com.crschnick.pdx_unlimiter.eu4.parser.ValueNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Ck3Transformer {

    public static final NodeTransformer TRANSFORMER = createCk3Transformer();
    public static final NodeTransformer META_TRANSFORMER = createCk3MetaTransformer();

    private static boolean isDateEntry(String key) {
        return key.endsWith("date");
    }

    private static NodeTransformer createCk3Transformer() {
        List<NodeTransformer> t = new ArrayList<>();

        t.add(DateTransformer.recursive(DateTransformer.CK3, Ck3Transformer::isDateEntry));
        t.add(BooleanTransformer.recursive());
        t.add(ColorTransformer.RECURSIVE_TRANSFORMER);
        t.add(new NodeTransformer() {
            @Override
            public void transform(Node node) {
                Node.getNodeArray(node).remove(Node.getKeyValueNodeForKey(node, "vassal_contracts"));
            }

            @Override
            public void reverse(Node node) {

            }
        });

        t.add(new CollectNodesTransformer("triggered_event", "triggered_events"));
        return new ChainTransformer(t);
    }

    private static NodeTransformer createCk3MetaTransformer() {
        List<NodeTransformer> t = new ArrayList<>();
        t.add(DateTransformer.recursive(DateTransformer.CK3, Ck3Transformer::isDateEntry));
        t.add(BooleanTransformer.recursive());
        t.add(new DefaultValueTransformer("mods", new ArrayNode()));
        return new ChainTransformer(t);
    }
}

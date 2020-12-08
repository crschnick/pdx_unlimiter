package com.crschnick.pdx_unlimiter.core.format.ck3;

import com.crschnick.pdx_unlimiter.core.format.*;
import com.crschnick.pdx_unlimiter.core.parser.ArrayNode;
import com.crschnick.pdx_unlimiter.core.parser.Node;

import java.util.ArrayList;
import java.util.List;

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

package com.crschnick.pdx_unlimiter.eu4.format.stellaris;

import com.crschnick.pdx_unlimiter.eu4.format.*;
import com.crschnick.pdx_unlimiter.eu4.format.hoi4.CountryTransformer;
import com.crschnick.pdx_unlimiter.eu4.parser.ArrayNode;
import com.crschnick.pdx_unlimiter.eu4.parser.KeyValueNode;
import com.crschnick.pdx_unlimiter.eu4.parser.Node;
import com.crschnick.pdx_unlimiter.eu4.parser.ValueNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StellarisTransformer {

    public static final NodeTransformer TRANSFORMER = createStellarisTransformer();
    public static final NodeTransformer META_TRANSFORMER = createMetaTransformer();

    private static boolean isDateEntry(String key) {
        return key.endsWith("date");
    }

    private static NodeTransformer createStellarisTransformer() {
        List<NodeTransformer> t = new ArrayList<>();
        t.add(new RecursiveTransformer((n) -> {
            if (n instanceof KeyValueNode && isDateEntry(Node.getKeyValueNode(n).getKeyName())) {
                Node val = Node.getKeyValueNode(n).getNode();
                return val instanceof ValueNode &&
                        (((ValueNode) val).getValue() instanceof String || ((ValueNode) val).getValue() instanceof Long);
            } else {
                return false;
            }
        }, DateTransformer.STELLARIS));

        t.add(new RecursiveTransformer((n) -> {
            if (n instanceof KeyValueNode) {
                Node val = Node.getKeyValueNode(n).getNode();
                return val instanceof ValueNode &&
                        (((ValueNode) val).getValue() instanceof String && (Node.getString(val).equals("yes") || Node.getString(val).equals("no")));
            } else {
                return false;
            }
        }, new BooleanTransformer()));


        t.add(new CollectNodesTransformer("nebula", "nebulas"));
        t.add(new CollectNodesTransformer("saved_event_target", "saved_event_targets"));
        t.add(new CollectNodesTransformer("used_color", "used_colors"));
        t.add(new CollectNodesTransformer("used_species_names", "used_species_names"));
        t.add(new CollectNodesTransformer("used_species_portrait", "used_species_portraits"));

        t.add(new RenameKeyTransformer("global_ship_design", "global_ship_designs"));
        t.add(new RenameKeyTransformer("deposit", "deposits"));
        t.add(new RenameKeyTransformer("ship_design", "ship_designs"));
        t.add(new RenameKeyTransformer("fleet_template", "fleet_templates"));
        t.add(new RenameKeyTransformer("ambient_object", "ambient_objects"));
        t.add(new RenameKeyTransformer("galactic_object", "galactic_objects"));
        t.add(new RenameKeyTransformer("pop", "pops"));
        t.add(new RenameKeyTransformer("country", "countries"));
        t.add(new RenameKeyTransformer("fleet", "fleets"));
        t.add(new RenameKeyTransformer("army", "armies"));
        t.add(new RenameKeyTransformer("war", "wars"));

        return new ChainTransformer(t);
    }

    private static final NodeTransformer createMetaTransformer() {
        List<NodeTransformer> t = new ArrayList<>();
        t.add(new RecursiveTransformer((n) -> {
            if (n instanceof KeyValueNode) {
                Node val = Node.getKeyValueNode(n).getNode();
                return val instanceof ValueNode &&
                        (((ValueNode) val).getValue() instanceof String && (Node.getString(val).equals("yes") || Node.getString(val).equals("no")));
            } else {
                return false;
            }
        }, new BooleanTransformer()));

        t.add(new SubnodeTransformer(Map.of(new String[]{"date"}, DateTransformer.STELLARIS), true));
        t.add(new DefaultValueTransformer("ironman", new ValueNode(false)));
        t.add(new DefaultValueTransformer("required_dlcs", new ArrayNode()));
        return new ChainTransformer(t);
    }
}

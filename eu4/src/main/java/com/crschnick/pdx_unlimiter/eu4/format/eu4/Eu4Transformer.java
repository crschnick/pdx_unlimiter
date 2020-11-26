package com.crschnick.pdx_unlimiter.eu4.format.eu4;

import com.crschnick.pdx_unlimiter.eu4.format.*;
import com.crschnick.pdx_unlimiter.eu4.parser.ArrayNode;
import com.crschnick.pdx_unlimiter.eu4.parser.KeyValueNode;
import com.crschnick.pdx_unlimiter.eu4.parser.Node;
import com.crschnick.pdx_unlimiter.eu4.parser.ValueNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Eu4Transformer {

    public static final NodeTransformer GAMESTATE_TRANSFORMER = createEu4Transformer();
    public static final NodeTransformer META_TRANSFORMER = createMetaTransformer();

    private static NodeTransformer createSettingsTransformer() {
        List<NodeTransformer> t = new ArrayList<>();

        Map<Integer, String> names = new HashMap<>();
        names.put(0, "difficulty");
        names.put(2, "province_values");
        names.put(31, "limited_country_forming");
        names.put(22, "fantasy_nations");
        names.put(14, "lucky_nations");
        t.add(new ArrayNameTransformer(names));

        t.add(new SubnodeTransformer(Map.of(new String[]{"difficulty"}, new EnumNameTransformer(
                Map.of(-1, "very_easy", 0, "easy", 1, "normal", 2, "hard", 3, "very_hard"))),
                true));

        t.add(new SubnodeTransformer(Map.of(new String[]{"limited_country_forming"}, new EnumNameTransformer(
                Map.of(0, "no", 1, "yes"))),
                true));

        t.add(new SubnodeTransformer(Map.of(new String[]{"fantasy_nations"}, new EnumNameTransformer(
                Map.of(0, "never", 1, "rare", 2, "uncommon", 3, "frequent"))),
                true));

        t.add(new SubnodeTransformer(Map.of(new String[]{"lucky_nations"}, new EnumNameTransformer(
                Map.of(0, "historical", 1, "none", 2, "random"))),
                true));

        t.add(new SubnodeTransformer(Map.of(new String[]{"province_values"}, new EnumNameTransformer(
                Map.of(0, "normal", 1, "flat", 2, "random"))),
                true));

        return new SubnodeTransformer(Map.of(new String[]{"gameplaysettings", "setgameplayoptions"}, new ChainTransformer(t)), false);
    }

    private static boolean isDateEntry(String key) {
        if (key.equals("expiry_date")) {
            return false;
        }

        return key.endsWith("date");
    }

    private static NodeTransformer createEu4Transformer() {
        List<NodeTransformer> t = new ArrayList<>();
        t.add(new RecursiveTransformer((n) -> {
            if (n instanceof KeyValueNode && isDateEntry(Node.getKeyValueNode(n).getKeyName())) {
                Node val = Node.getKeyValueNode(n).getNode();
                return val instanceof ValueNode &&
                        (((ValueNode) val).getValue() instanceof String || ((ValueNode) val).getValue() instanceof Long);
            } else {
                return false;
            }
        }, DateTransformer.EU4));

        t.add(new RecursiveTransformer((n) -> {
            if (n instanceof KeyValueNode) {
                Node val = Node.getKeyValueNode(n).getNode();
                return val instanceof ValueNode &&
                        (((ValueNode) val).getValue() instanceof String && (Node.getString(val).equals("yes") || Node.getString(val).equals("no")));
            } else {
                return false;
            }
        }, new BooleanTransformer()));

        t.add(new SubnodeTransformer(Map.of(new String[]{"flags", "*"}, DateTransformer.EU4), true));
        t.add(createSettingsTransformer());

        t.add(new CollectNodesTransformer("active_war", "active_wars"));
        t.add(new CollectNodesTransformer("previous_war", "previous_wars"));
        t.add(new CollectNodesTransformer("rebel_faction", "rebel_factions"));
        t.add(new CollectNodesTransformer("trade_league", "trade_leagues"));
        t.add(new RenameKeyTransformer("trade", "trade_nodes"));

        t.add(new RenameKeyTransformer("religion_instance_data", "religion_data"));
        t.add(new SubnodeTransformer(Map.of(new String[]{"religion_data", "muslim"}, new CollectNodesTransformer("relation", "relations")), false));
        t.add(new SubnodeTransformer(Map.of(new String[]{"religion_data", "catholic"}, new CollectNodesTransformer("cardinal", "cardinals")), false));

        t.add(new SubnodeTransformer(Map.of(new String[]{"countries", "*"}, new EventTransformer()), false));
        t.add(new CountryTransformer());

        // diplomacy
        t.add(new SubnodeTransformer(Map.of(new String[]{"diplomacy"}, new CollectNodesTransformer("casus_belli", "casus_bellis")), false));
        t.add(new SubnodeTransformer(Map.of(new String[]{"diplomacy"}, new CollectNodesTransformer("dependency", "dependencies")), false));
        t.add(new SubnodeTransformer(Map.of(new String[]{"diplomacy"}, new CollectNodesTransformer("transfer_trade_power", "transfer_trade_powers")), false));
        t.add(new SubnodeTransformer(Map.of(new String[]{"diplomacy"}, new CollectNodesTransformer("guarantee", "guarantees")), false));
        t.add(new SubnodeTransformer(Map.of(new String[]{"diplomacy"}, new CollectNodesTransformer("alliance", "alliances")), false));
        t.add(new SubnodeTransformer(Map.of(new String[]{"diplomacy"}, new CollectNodesTransformer("royal_marriage", "royal_marriages")), false));

        t.add(new SubnodeTransformer(Map.of(new String[]{"trade_nodes"}, new CollectNodesTransformer("node", "trade_nodes")), false));
        t.add(new SubnodeTransformer(Map.of(new String[]{"provinces"}, new ProvincesTransformer()), false));
        t.add(new SubnodeTransformer(Map.of(new String[]{"provinces"},
                new ArrayTransformer(
                        new CollectNodesTransformer("unit", "units"))), false));
        //t.add(new SubnodeTransformer(Map.of(new String[] {"multi_player"}, new BooleanTransformer()), true));

        t.add(new SubnodeTransformer(Map.of(new String[]{"map_area_data", "*", "state"}, new CollectNodesTransformer("country_state", "country_states")), false));

        t.add(new DefaultValueTransformer("mod_enabled", new ArrayNode()));

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

        t.add(new SubnodeTransformer(Map.of(new String[]{"date"}, DateTransformer.EU4), true));
        t.add(new DefaultValueTransformer("is_random_new_world", new ValueNode(false)));
        t.add(new DefaultValueTransformer("ironman", new ValueNode(false)));
        t.add(new DefaultValueTransformer("dlc_enabled", new ArrayNode()));
        return new ChainTransformer(t);
    }
}

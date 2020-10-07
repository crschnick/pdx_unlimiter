package com.crschnick.pdx_unlimiter.eu4.format.eu4;

import com.crschnick.pdx_unlimiter.eu4.format.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Eu4Transformer {

    public static final NodeTransformer GAMESTATE_TRANSFORMER = createEu4Transformer();
    public static final NodeTransformer META_TRANSFORMER = createMetaTransformer();


    private static NodeTransformer createEu4Transformer() {
        List<NodeTransformer> t = new ArrayList<>();
        t.add(new CollectNodesTransformer("ongoing_war", "ongoing_wars"));
        t.add(new CollectNodesTransformer("previous_war", "ended_wars"));
        t.add(new CollectNodesTransformer("rebel_faction", "rebel_factions"));
        t.add(new CollectNodesTransformer("trade_league", "trade_leagues"));
        t.add(new RenameKeyTransformer("trade", "trade_nodes"));

        t.add(new RenameKeyTransformer("religion_instance_data", "religion_data"));
        t.add(new SubnodeTransformer(Map.of(new String[] {"religion_data", "muslim"}, new CollectNodesTransformer("relation", "relations")), false));
        t.add(new SubnodeTransformer(Map.of(new String[] {"religion_data", "catholic"}, new CollectNodesTransformer("cardinal", "cardinals")), false));

        t.add(new SubnodeTransformer(Map.of(new String[] {"countries", "*"}, new EventTransformer()), false));

        // diplomacy
        t.add(new SubnodeTransformer(Map.of(new String[] {"diplomacy"}, new CollectNodesTransformer("casus_belli", "casus_bellis")), false));
        t.add(new SubnodeTransformer(Map.of(new String[] {"diplomacy"}, new CollectNodesTransformer("dependency", "dependencies")), false));
        t.add(new SubnodeTransformer(Map.of(new String[] {"diplomacy"}, new CollectNodesTransformer("transfer_trade_power", "transfer_trade_powers")), false));
        t.add(new SubnodeTransformer(Map.of(new String[] {"diplomacy"}, new CollectNodesTransformer("guarantee", "guarantees")), false));
        t.add(new SubnodeTransformer(Map.of(new String[] {"diplomacy"}, new CollectNodesTransformer("alliance", "alliances")), false));
        t.add(new SubnodeTransformer(Map.of(new String[] {"diplomacy"}, new CollectNodesTransformer("royal_marriage", "royal_marriages")), false));

        t.add(new SubnodeTransformer(Map.of(new String[] {"trade_nodes"}, new CollectNodesTransformer("node", "trade_nodes")), false));
        t.add(new SubnodeTransformer(Map.of(new String[] {"provinces"}, new ProvincesTransformer()), false));
        t.add(new SubnodeTransformer(Map.of(new String[] {"provinces"},
                new ArrayTransformer(
                        new CollectNodesTransformer("unit", "units"))), false));
        //t.add(new SubnodeTransformer(Map.of(new String[] {"multi_player"}, new BooleanTransformer()), true));

        t.add(new SubnodeTransformer(Map.of(new String[] {"map_area_data", "*", "state"}, new CollectNodesTransformer("country_state", "country_states")), false));

        return new ChainTransformer(t);
    }

    private static final NodeTransformer createMetaTransformer() {

        List<NodeTransformer> t = new ArrayList<>();
        t.add(new SubnodeTransformer(Map.of(new String[] {"date"}, new DateTransformer()), true));
        return new ChainTransformer(t);
    }
}

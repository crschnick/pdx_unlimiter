package com.paradox_challenges.eu4_unlimiter.converter;

import com.paradox_challenges.eu4_unlimiter.format.*;
import com.paradox_challenges.eu4_unlimiter.format.eu4.Eu4Transformer;
import com.paradox_challenges.eu4_unlimiter.format.eu4.ProvincesTransformer;
import com.paradox_challenges.eu4_unlimiter.parser.GamedataParser;
import com.paradox_challenges.eu4_unlimiter.parser.Node;
import com.paradox_challenges.eu4_unlimiter.parser.eu4.Eu4IronmanParser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Eu4Converter extends SavegameConverter {

    private static NodeTransformer createEu4Transformer() {
        List<NodeTransformer> t = new ArrayList<>();
        t.add(new CollectNodesTransformer("ongoing_war", "ongoing_wars"));
        t.add(new CollectNodesTransformer("previous_war", "ended_wars"));
        t.add(new CollectNodesTransformer("rebel_faction", "rebel_factions"));
        t.add(new CollectNodesTransformer("trade_league", "trade_leagues"));
        t.add(new RenameKeyTransformer("trade", "trade_nodes"));
        t.add(new RenameKeyTransformer("religion_instance_data", "religion_data"));
        t.add(new SubnodeTransformer(Map.of(new String[] {"trade_nodes"}, new CollectNodesTransformer("node", "trade_nodes")), false));
        t.add(new SubnodeTransformer(Map.of(new String[] {"provinces"}, new ProvincesTransformer()), false));
        t.add(new SubnodeTransformer(Map.of(new String[] {"multi_player"}, new BooleanTransformer()), true));
        return new ChainTransformer(t);
    }

    public Eu4Converter() {
        super(createEu4Transformer(), "gamestate", "ongoing_wars", "ended_wars", "provinces", "countries", "trade_nodes", "rebel_factions", "active_advisors", "map_area_data", "religions", "diplomacy", "inflation_statistics", "ai", "religion_data");

        //super(createEu4Transformer(), "gamestate", "ongoing_wars", "ended_wars", "provinces", "military", "religion", "countries", "trade_nodes", "rebel_factions", "province_count_over_time", "country_scores_over_time", "yearly_income_over_time", "inflation_over_time", "active_advisors");
    }
}

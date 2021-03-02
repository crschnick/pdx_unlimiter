package com.crschnick.pdx_unlimiter.core.info.ck3;

import com.crschnick.pdx_unlimiter.core.node.Node;
import com.crschnick.pdx_unlimiter.core.node.ValueNode;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Ck3Title {

    private String name;
    private Ck3CoatOfArms coatOfArms;

    public Ck3Title() {
    }

    public Ck3Title(String name, Ck3CoatOfArms coatOfArms) {
        this.name = name;
        this.coatOfArms = coatOfArms;
    }

    public static Map<Long, Ck3Title> createTitleMap(Node node, Map<Long, Ck3CoatOfArms> coaMap) {
        var tts = node.getNodeForKey("landed_titles").getNodeForKey("landed_titles");
        var map = new HashMap<Long, Ck3Title>();
        tts.forEach((k, v) -> {
            fromNode(v, coaMap).ifPresent(t -> {
                map.put(Long.parseLong(k), t);
            });
        });
        return map;
    }

    private static Optional<Ck3Title> fromNode(Node n, Map<Long, Ck3CoatOfArms> coaMap) {
        // If node is "none"
        if (n instanceof ValueNode) {
            return Optional.empty();
        }

        var name = n.getNodeForKey("name").getString();
        var coaId = n.getNodeForKey("coat_of_arms_id").getLong();
        var coatOfArms = coaMap.get(coaId);
        return Optional.of(new Ck3Title(name, coatOfArms));
    }

    public String getName() {
        return name;
    }

    public Ck3CoatOfArms getCoatOfArms() {
        return coatOfArms;
    }
}

package com.crschnick.pdx_unlimiter.core.info.ck3;

import com.crschnick.pdx_unlimiter.core.node.Node;
import com.crschnick.pdx_unlimiter.core.node.ValueNode;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class Ck3Title {

    private long id;
    private String name;
    private Ck3CoatOfArms coatOfArms;

    public Ck3Title() {
    }

    public Ck3Title(long id, String name, Ck3CoatOfArms coatOfArms) {
        this.id = id;
        this.name = name;
        this.coatOfArms = coatOfArms;
    }

    public static Map<Long, Ck3Title> createTitleMap(Node node, Map<Long, Ck3CoatOfArms> coaMap) {
        return node.getNodeArray().stream()
                .map(n -> Ck3Title.fromNode(n, coaMap))
                .flatMap(Optional::stream)
                .collect(Collectors.toMap(t -> t.id, t -> t));
    }

    private static Optional<Ck3Title> fromNode(Node n, Map<Long, Ck3CoatOfArms> coaMap) {
        // If node is "none"
        if (n instanceof ValueNode) {
            return Optional.empty();
        }

        var id = n.getNodeForKey("id").getLong();
        var name = n.getNodeForKey("name").getString();
        var coaId = n.getNodeForKey("coat_of_arms_id").getLong();
        var coatOfArms = coaMap.get(id);
        return Optional.of(new Ck3Title(id, name, coatOfArms));
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Ck3CoatOfArms getCoatOfArms() {
        return coatOfArms;
    }
}

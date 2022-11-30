package com.crschnick.pdxu.model.ck3;

import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.model.coa.CoatOfArms;
import com.crschnick.pdxu.model.GameColor;

import java.util.*;

public class Ck3Title {

    private long id;
    private String key;
    private String name;
    private GameColor color;
    private CoatOfArms coatOfArms;
    private Type type;
    public Ck3Title() {
    }

    public Ck3Title(long id, String key, String name, GameColor color, CoatOfArms coatOfArms, Type type) {
        this.id = id;
        this.key = key;
        this.name = name;
        this.color = color;
        this.coatOfArms = coatOfArms;
        this.type = type;
    }

    public static Map<Long, Ck3Title> createTitleMap(Node node, Map<Long, CoatOfArms> coaMap) {
        var tts = node.getNodeForKey("landed_titles").getNodeForKey("landed_titles");
        var map = new HashMap<Long, Ck3Title>();
        tts.forEach((k, v) -> {
            try {
                var id = Long.parseLong(k);
                fromNode(id, v, coaMap).ifPresent(t -> {
                    map.put(id, t);
                });
            } catch (NumberFormatException ignored) {
            }
        });
        return map;
    }

    private static Optional<Ck3Title> fromNode(long id, Node n, Map<Long, CoatOfArms> coaMap) {
        // If node is "none"
        if (n.isValue()) {
            return Optional.empty();
        }

        var name = n.getNodeForKeyIfExistent("name").map(Node::getString).orElse(null);
        var key = n.getNodeForKeyIfExistent("key").map(Node::getString).orElse(null);
        if (name == null || key == null) {
            return Optional.empty();
        }

        var type = Arrays.stream(Type.values())
                .filter(t -> key.startsWith(t.prefix + "_"))
                .findFirst()
                .orElse(null);
        if (type == null) {
            return Optional.empty();
        }

        CoatOfArms coatOfArms;
        if (n.hasKey("coat_of_arms_id")) {
            var coaId = n.getNodeForKey("coat_of_arms_id").getLong();
            coatOfArms = coaMap.get(coaId);
        } else {
            coatOfArms = CoatOfArms.empty();
        }

        var color = n.getNodeForKeyIfExistent("color")
                .map(Node::getTaggedNode)
                .map(GameColor::fromColorNode)
                .orElse(null);

        return Optional.of(new Ck3Title(id, key, name, color, coatOfArms, type));
    }

    public Type getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ck3Title ck3Title = (Ck3Title) o;
        return id == ck3Title.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public String getName() {
        return name;
    }

    public CoatOfArms getCoatOfArms() {
        return coatOfArms;
    }

    public Optional<GameColor> getColor() {
        return Optional.ofNullable(color);
    }

    public String getKey() {
        return key;
    }

    public long getId() {
        return id;
    }

    public static enum Type {

        BARONY("b"),
        COUNTY("c"),
        DUCHY("d"),
        KINGDOM("k"),
        EMPIRE("e"),
        OTHER("x");

        private String prefix;

        Type(String prefix) {
            this.prefix = prefix;
        }
    }
}

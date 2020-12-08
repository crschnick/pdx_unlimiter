package com.crschnick.pdx_unlimiter.core.data;

import com.crschnick.pdx_unlimiter.core.parser.KeyValueNode;
import com.crschnick.pdx_unlimiter.core.parser.Node;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class Eu4Tag {

    private String tag;
    private int mapColor;
    private int countryColor;
    private Optional<String> name;

    public Eu4Tag(String tag, int mapColor, int countryColor, Optional<String> name) {
        this.tag = tag;
        this.mapColor = mapColor;
        this.countryColor = countryColor;
        this.name = name;
    }

    public static Eu4Tag fromNode(Node n) {
        KeyValueNode kv = Node.getKeyValueNode(n);
        List<Node> mc = Node.getNodeArray(Node.getNodeForKey(Node.getNodeForKey(kv.getNode(), "colors"), "map_color"));
        int mColor = (Node.getInteger(mc.get(0)) << 24) + (Node.getInteger(mc.get(1)) << 16) + (Node.getInteger(mc.get(2)) << 8);
        List<Node> cc = Node.getNodeArray(Node.getNodeForKey(Node.getNodeForKey(kv.getNode(), "colors"), "country_color"));
        int cColor = (Node.getInteger(mc.get(0)) << 24) + (Node.getInteger(cc.get(1)) << 16) + (Node.getInteger(cc.get(2)) << 8);
        Optional<String> name = Node.hasKey(kv.getNode(), "name") ? Optional.of(Node.getString(Node.getNodeForKey(kv.getNode(), "name"))) : Optional.empty();
        return new Eu4Tag(kv.getKeyName(), mColor, cColor, name);
    }

    public static Eu4Tag getTag(Set<Eu4Tag> tags, String name) {
        return tags.stream().filter(t -> t.tag.equals(name)).findFirst().get();
    }

    public String getTag() {
        return tag;
    }

    public int getMapColor() {
        return mapColor;
    }

    public int getCountryColor() {
        return countryColor;
    }

    public boolean isCustom() {
        return name.isPresent();
    }

    public String getName() {
        if (!isCustom()) {
            throw new IllegalStateException();
        }

        return name.get();
    }
}

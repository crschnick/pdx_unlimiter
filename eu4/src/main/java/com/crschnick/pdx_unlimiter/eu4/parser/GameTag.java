package com.crschnick.pdx_unlimiter.eu4.parser;

import java.awt.*;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class GameTag {

    private String tag;
    private int mapColor;
    private int countryColor;
    private Optional<String> name;

    public GameTag(String tag, int mapColor, int countryColor, Optional<String> name) {
        this.tag = tag;
        this.mapColor = mapColor;
        this.countryColor = countryColor;
        this.name = name;
    }

    public static GameTag fromNode(Node n) {
        KeyValueNode kv = Node.getKeyValueNode(n);
        List<Node> mc = Node.getNodeArray(Node.getNodeForKey(Node.getNodeForKey(kv.getNode(), "colors"), "map_color"));
        int mColor = (Node.getInteger(mc.get(0)) << 24) + (Node.getInteger(mc.get(1)) << 16) + (Node.getInteger(mc.get(2)) << 8);
        List<Node> cc = Node.getNodeArray(Node.getNodeForKey(Node.getNodeForKey(kv.getNode(), "colors"), "country_color"));
        int cColor = (Node.getInteger(mc.get(0)) << 24) + (Node.getInteger(cc.get(1)) << 16) + (Node.getInteger(cc.get(2)) << 8);
        Optional<String> name = Node.hasKey(kv.getNode(), "name") ? Optional.of(Node.getString(Node.getNodeForKey(kv.getNode(), "name"))) : Optional.empty();
        return new GameTag(kv.getKeyName(), mColor, cColor, name);
    }

    public static GameTag getTag(Set<GameTag> tags, String name) {
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

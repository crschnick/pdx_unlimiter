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
    private String name;

    public Eu4Tag() {
    }

    public Eu4Tag(String tag, int mapColor, int countryColor, String name) {
        this.tag = tag;
        this.mapColor = mapColor;
        this.countryColor = countryColor;
        this.name = name;
    }

    public static Eu4Tag fromNode(Node n) {
        KeyValueNode kv = n.getKeyValueNode();
        List<Node> mc = kv.getNode().getNodeForKey("colors").getNodeForKey("map_color").getNodeArray();
        int mColor = (mc.get(0).getInteger() << 24) + (mc.get(1).getInteger() << 16) + (mc.get(2).getInteger() << 8);
        List<Node> cc = kv.getNode().getNodeForKey("colors").getNodeForKey("country_color").getNodeArray();
        int cColor = (cc.get(0).getInteger() << 24) + (cc.get(1).getInteger() << 16) + (cc.get(2).getInteger() << 8);
        String name = kv.getNode().hasKey("name") ?
                kv.getNode().getNodeForKey("name").getString() : null;
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
        return name != null;
    }

    public String getName() {
        if (!isCustom()) {
            throw new IllegalStateException();
        }

        return name;
    }
}

package com.crschnick.pdx_unlimiter.core.info.eu4;

import com.crschnick.pdx_unlimiter.core.node.Node;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public final class Eu4Tag {

    private static final Pattern CUSTOM_FLAG_TAG_PATTERN = Pattern.compile("\\w\\d\\d");

    public static enum FlagType {
        NORMAL,
        COLONIAL_FLAG,
        CUSTOM_FLAG
    }

    public static class ColonialData {
        private String name;
        private String overlord;

        public ColonialData() {}

        public ColonialData(String name, String overlord) {
            this.name = name;
            this.overlord = overlord;
        }

        public String getName() {
            return name;
        }

        public String getOverlord() {
            return overlord;
        }
    }

    public static class CustonNationData {
        private String name;
        private int points;

        public CustonNationData() {
        }

        public CustonNationData(String name, int points) {
            this.name = name;
            this.points = points;
        }

        public String getName() {
            return name;
        }

        public int getPoints() {
            return points;
        }
    }

    private FlagType flagType;
    private String tag;
    private int mapColor;
    private int countryColor;
    private String name;
    private Object data;

    public Eu4Tag() {
    }

    public Eu4Tag(FlagType flagType, String tag, int mapColor, int countryColor, String name, Object data) {
        this.flagType = flagType;
        this.tag = tag;
        this.mapColor = mapColor;
        this.countryColor = countryColor;
        this.name = name;
        this.data = data;
    }

    public static Eu4Tag fromNode(String tag, Node n) {
        List<Node> mc = n.getNodeForKey("colors").getNodeForKey("map_color").getNodeArray();
        int mColor = (mc.get(0).getInteger() << 24) + (mc.get(1).getInteger() << 16) + (mc.get(2).getInteger() << 8);
        List<Node> cc = n.getNodeForKey("colors").getNodeForKey("country_color").getNodeArray();
        int cColor = (cc.get(0).getInteger() << 24) + (cc.get(1).getInteger() << 16) + (cc.get(2).getInteger() << 8);
        String name = n.hasKey("name") ?
                n.getNodeForKey("name").getString() : null;

        FlagType t;
        Object data;
        if (n.hasKey("colonial_parent")) {
            t = FlagType.COLONIAL_FLAG;
            data = new ColonialData(
                    n.getNodeForKey("name").getString(),
                    n.getNodeForKey("colonial_parent").getString());

        } else if(CUSTOM_FLAG_TAG_PATTERN.matcher(tag).matches()) {
            t = FlagType.CUSTOM_FLAG;
            data = new CustonNationData(
                    n.getNodeForKey("name").getString(),
                    n.getNodeForKey("custom_nation_points").getInteger());
        } else {
            t = FlagType.NORMAL;
            data = null;
        }

        return new Eu4Tag(t, tag, mColor, cColor, name, data);
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

    public FlagType getType() {
        return flagType;
    }

    public Object getData() {
        return data;
    }
}

package com.crschnick.pdxu.model.eu4;

import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.model.GameColor;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class Eu4Tag {

    public static final String INVALID_TAG_ID = "---";

    private static final Pattern COLONIAL_FLAG_TAG_PATTERN = Pattern.compile("C\\d\\d");
    private static final Pattern OBSERVER_FLAG_TAG_PATTERN = Pattern.compile("O\\d\\d");

    private FlagType flagType;
    private String tag;
    private GameColor mapColor;
    private GameColor countryColor;
    private String name;
    private ColonialFlagData colonialFlagData;
    private CustomFlagData customFlagData;

    public Eu4Tag() {
    }

    public Eu4Tag(FlagType flagType, String tag, GameColor mapColor, GameColor countryColor, String name, ColonialFlagData colonialFlagData, CustomFlagData customFlagData) {
        this.flagType = flagType;
        this.tag = tag;
        this.mapColor = mapColor;
        this.countryColor = countryColor;
        this.name = name;
        this.colonialFlagData = colonialFlagData;
        this.customFlagData = customFlagData;
    }

    public static Eu4Tag fromNode(String tag, Node n) {
        var mColor = n.getNodeForKeysIfExistent("colors", "map_color").map(GameColor::fromRgbArray).orElse(GameColor.BLACK);
        var cColor = n.getNodeForKeysIfExistent("colors", "country_color").map(GameColor::fromRgbArray).orElse(GameColor.BLACK);
        String name = n.hasKey("name") ? n.getNodeForKey("name").getString() : null;

        FlagType t;
        ColonialFlagData colonialFlagData = null;
        CustomFlagData customFlagData = null;
        if (COLONIAL_FLAG_TAG_PATTERN.matcher(tag).matches() && n.hasKey("colonial_parent")) {
            t = FlagType.COLONIAL_FLAG;
            colonialFlagData = new ColonialFlagData(
                    n.getNodeForKey("colonial_parent").getString());

        } else if (n.getNodeForKey("colors").hasKey("custom_colors")) {
            t = FlagType.CUSTOM_FLAG;
            var col = n.getNodeForKey("colors").getNodeForKey("custom_colors");
            customFlagData = new CustomFlagData(
                    col.getNodeForKey("flag").getInteger(),
                    col.getNodeForKey("color").getInteger(),
                    col.getNodeForKey("symbol_index").getInteger(),
                    col.getNodeForKey("flag_colors").getNodeArray().stream()
                            .map(Node::getInteger)
                            .collect(Collectors.toList()));
        } else if (OBSERVER_FLAG_TAG_PATTERN.matcher(tag).matches() || tag.equals(INVALID_TAG_ID)) {
            t = FlagType.NO_FLAG;
        } else {
            t = FlagType.NORMAL;
        }

        return new Eu4Tag(t, tag, mColor, cColor, name, colonialFlagData, customFlagData);
    }

    public static Eu4Tag getTag(List<Eu4Tag> tags, String name) {
        return tags.stream().filter(t -> t.tag.equals(name)).findFirst().get();
    }

    public String getTag() {
        return tag;
    }

    public GameColor getMapColor() {
        return mapColor;
    }

    public GameColor getCountryColor() {
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

    public ColonialFlagData getColonialData() {
        return colonialFlagData;
    }

    public CustomFlagData getCustomData() {
        return customFlagData;
    }

    public static enum FlagType {
        NO_FLAG,
        NORMAL,
        COLONIAL_FLAG,
        CUSTOM_FLAG
    }

    public static class ColonialFlagData {
        private String overlord;

        public ColonialFlagData() {
        }

        public ColonialFlagData(String overlord) {
            this.overlord = overlord;
        }

        public String getOverlord() {
            return overlord;
        }
    }

    public static class CustomFlagData {
        private int flagId;
        private int colorId;
        private int symbolId;
        private List<Integer> flagColors;

        public CustomFlagData() {
        }

        public CustomFlagData(int flagId, int colorId, int symbolId, List<Integer> flagColors) {
            this.flagId = flagId;
            this.colorId = colorId;
            this.symbolId = symbolId;
            this.flagColors = flagColors;
        }

        public int getFlagId() {
            return flagId;
        }

        public int getColorId() {
            return colorId;
        }

        public int getSymbolId() {
            return symbolId;
        }

        public List<Integer> getFlagColors() {
            return flagColors;
        }
    }
}

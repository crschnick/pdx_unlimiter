package com.crschnick.pdx_unlimiter.core.parser;

import java.util.List;

public class ColorNode extends Node {

    public static boolean isColorName(String v) {
        return v.equals("rgb") || v.equals("hsv") || v.equals("hsv360");
    }

    private String colorName;
    private List<ValueNode> values;

    public ColorNode(String colorName, List<ValueNode> values) {
        this.colorName = colorName;
        this.values = values;
    }

    public String getColorName() {
        return colorName;
    }

    public List<ValueNode> getValues() {
        return values;
    }
}

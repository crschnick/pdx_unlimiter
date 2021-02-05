package com.crschnick.pdx_unlimiter.core.parser;

public class ColorNode extends Node {

    private String colorName;

    private int[] values;

    public ColorNode(String colorName, int[] values) {
        this.colorName = colorName;
        this.values = values;
    }

    public String getColorName() {
        return colorName;
    }

    public int[] getValues() {
        return values;
    }
}

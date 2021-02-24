package com.crschnick.pdx_unlimiter.core.node;

import com.crschnick.pdx_unlimiter.core.parser.NodeWriter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public final class ColorNode extends Node {

    private final String colorName;
    private final List<ValueNode> values;

    public ColorNode(String colorName, List<ValueNode> values) {
        this.colorName = colorName;
        this.values = values;
    }

    private static final byte[] RGB = "rgb".getBytes();
    private static final byte[] HSV = "hsv".getBytes();
    private static final byte[] HSV360 = "hsv360".getBytes();

    public static boolean isColorName(NodeContext ctx, int begin, int length) {
        if (length != 3 && length != 6) {
            return false;
        }

        if (ctx.getData()[begin] != 'r' && ctx.getData()[begin] != 'h') {
            return false;
        }

        return Arrays.equals(RGB, 0, RGB.length, ctx.getData(), begin, length) ||
                Arrays.equals(HSV, 0, HSV.length, ctx.getData(), begin, length) ||
                Arrays.equals(HSV360, 0, HSV360.length, ctx.getData(), begin, length);
    }

    public String getColorName() {
        return colorName;
    }

    public List<ValueNode> getValues() {
        return values;
    }

    @Override
    public void write(NodeWriter writer) throws IOException {
        writer.write(colorName);
        writer.write("= {");
        for (var v : values) {
            v.write(writer);
            writer.write(" ");
        }
        writer.write("}");
    }

    @Override
    public boolean isValue() {
        return false;
    }

    @Override
    public boolean isArray() {
        return false;
    }

    @Override
    public boolean isColor() {
        return true;
    }
}

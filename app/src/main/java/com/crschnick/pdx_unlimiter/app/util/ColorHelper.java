package com.crschnick.pdx_unlimiter.app.util;

import com.crschnick.pdx_unlimiter.app.core.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.installation.Game;
import com.crschnick.pdx_unlimiter.app.installation.GameInstallation;
import com.crschnick.pdx_unlimiter.core.info.SavegameInfo;
import com.crschnick.pdx_unlimiter.core.info.ck3.Ck3Tag;
import com.crschnick.pdx_unlimiter.core.info.stellaris.StellarisTag;
import com.crschnick.pdx_unlimiter.core.node.ColorNode;
import com.crschnick.pdx_unlimiter.core.node.Node;
import com.crschnick.pdx_unlimiter.core.node.ValueNode;
import com.crschnick.pdx_unlimiter.core.parser.TextFormatParser;
import javafx.scene.paint.Color;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ColorHelper {

    public static int pickClosestColor(int input, int... colors) {
        int minDist = Integer.MAX_VALUE;
        int cMin = -1;
        int counter = 0;
        for (int c : colors) {
            if (Math.abs(input - c) < minDist) {
                minDist = Math.abs(input - c);
                cMin = counter;
            }
            counter++;
        }
        return cMin;
    }

    public static int getRed(int color) {
        return (color & 0x00FF0000) >>> 16;
    }

    public static int getGreen(int color) {
        return (color & 0x0000FF00) >>> 8;
    }

    public static int getBlue(int color) {
        return (color & 0x000000FF);
    }

    public static int getAlpha(int color) {
        return (color & 0xFF000000) >>> 24;
    }

    public static java.awt.Color awtColorFromInt(int c, int alpha) {
        Color fx = ColorHelper.colorFromInt(c, alpha);
        return new java.awt.Color((float) fx.getRed(),
                (float) fx.getGreen(),
                (float) fx.getBlue(),
                (float) fx.getOpacity());
    }

    public static javafx.scene.paint.Color colorFromInt(int c, int alpha) {
        return Color.rgb(c >>> 24, (c >>> 16) & 255, (c >>> 8) & 255, alpha / 255.0);
    }

    public static int intFromColor(Color c) {
        return (0xFF << 24) + ((int) (c.getRed() * 0xFF) << 16) + ((int) (c.getGreen() * 0xFF) << 8) + ((int) (c.getBlue() * 0xFF));
    }

    public static ColorNode toColorNode(Color c) {
        return new ColorNode("rgb", List.of(
                new ValueNode(String.valueOf((int) (c.getRed() * 255))),
                new ValueNode(String.valueOf((int) (c.getGreen() * 255))),
                new ValueNode(String.valueOf((int) (c.getBlue() * 255)))));
    }

    public static Color fromColorNode(ColorNode node) {
        var c = node.getValues();
        switch (node.getColorName()) {
            case "hsv":
                return Color.hsb(
                        c.get(0).getDouble() * 360,
                        c.get(1).getDouble(),
                        c.get(2).getDouble());
            case "hsv360":
                return Color.hsb(
                        c.get(0).getDouble(),
                        c.get(1).getDouble() / 360.0,
                        c.get(2).getDouble() / 360.0);
            case "rgb":
                return Color.color(
                        c.get(0).getDouble() / 255.0,
                        c.get(1).getDouble() / 255.0,
                        c.get(2).getDouble() / 255.0);
        }

        throw new IllegalArgumentException();
    }

    private static Map<String, Color> loadPredefinedColors(Node node) {
        Map<String, Color> map = new HashMap<>();
        node.getNodeForKey("colors").forEach((k, v) -> {
            ColorNode colorData = (ColorNode) v.getNodeForKey("flag");
            map.put(k, fromColorNode(colorData));
        });
        return map;
    }

    private static Map<String, Color> loadPredefinedCk3Colors(Node node) {
        Map<String, Color> map = new HashMap<>();
        node.getNodeForKey("colors").forEach((k, v) -> {
            ColorNode colorData = (ColorNode) v;
            map.put(k, fromColorNode(colorData));
        });
        return map;
    }

    public static Map<String, Color> loadCk3(SavegameInfo<Ck3Tag> info) {
        var file = CascadeDirectoryHelper.openFile(
                Path.of("common").resolve("named_colors").resolve("default_colors.txt"),
                info,
                GameInstallation.ALL.get(Game.CK3)).get();
        try {
            Node node = TextFormatParser.textFileParser().parse(file);
            return loadPredefinedCk3Colors(node);
        } catch (Exception ex) {
            ErrorHandler.handleException(ex);
            return Map.of();
        }
    }

    public static Map<String, Color> loadStellarisColors(SavegameInfo<StellarisTag> info) {
        var file = CascadeDirectoryHelper.openFile(
                Path.of("flags").resolve("colors.txt"), info, GameInstallation.ALL.get(Game.STELLARIS)).get();

        try {
            Node node = TextFormatParser.textFileParser().parse(file);
            return loadPredefinedColors(node);
        } catch (Exception ex) {
            ErrorHandler.handleException(ex);
            return Map.of();
        }
    }
}

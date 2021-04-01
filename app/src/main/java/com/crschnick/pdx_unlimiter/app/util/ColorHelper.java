package com.crschnick.pdx_unlimiter.app.util;

import com.crschnick.pdx_unlimiter.app.core.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.installation.Game;
import com.crschnick.pdx_unlimiter.app.installation.GameInstallation;
import com.crschnick.pdx_unlimiter.core.info.GameColor;
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

    public static Color withAlpha(Color c, double alpha) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
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

    public static java.awt.Color toAwtColor(Color fx) {
        return new java.awt.Color((float) fx.getRed(),
                (float) fx.getGreen(),
                (float) fx.getBlue(),
                (float) fx.getOpacity());
    }

    public static int intFromColor(Color c) {
        return (0xFF << 24) + ((int) (c.getRed() * 0xFF) << 16) + ((int) (c.getGreen() * 0xFF) << 8) + ((int) (c.getBlue() * 0xFF));
    }

    public static ColorNode toColorNode(Color c) {
        return new ColorNode(GameColor.Type.RGB, List.of(
                new ValueNode(String.valueOf((int) (c.getRed() * 255))),
                new ValueNode(String.valueOf((int) (c.getGreen() * 255))),
                new ValueNode(String.valueOf((int) (c.getBlue() * 255)))));
    }

    public static Color fromGameColor(GameColor color) {
        var c = color.getValues();
        return switch (color.getType()) {
            case HSV -> Color.hsb(
                    Double.parseDouble(c.get(0)) * 360,
                    Double.parseDouble(c.get(1)),
                    Double.parseDouble(c.get(2)));
            case HSV360 -> Color.hsb(
                    Double.parseDouble(c.get(0)),
                    Double.parseDouble(c.get(1)) / 360.0,
                    Double.parseDouble(c.get(2)) / 360.0);
            case RGB -> Color.color(
                    Double.parseDouble(c.get(0)) / 255.0,
                    Double.parseDouble(c.get(1)) / 255.0,
                    Double.parseDouble(c.get(2)) / 255.0);
            case HEX -> Color.valueOf("#" + color.getValues().get(0));
        };
    }

    private static Map<String, Color> loadPredefinedColors(Node node) {
        Map<String, Color> map = new HashMap<>();
        node.getNodeForKey("colors").forEach((k, v) -> {
            ColorNode colorData = (ColorNode) v.getNodeForKey("flag");
            map.put(k, fromGameColor(GameColor.fromColorNode(colorData)));
        });
        return map;
    }

    private static Map<String, Color> loadPredefinedCk3Colors(Node node) {
        Map<String, Color> map = new HashMap<>();
        node.getNodeForKey("colors").forEach((k, v) -> {
            ColorNode colorData = (ColorNode) v;
            map.put(k, fromGameColor(GameColor.fromColorNode(colorData)));
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

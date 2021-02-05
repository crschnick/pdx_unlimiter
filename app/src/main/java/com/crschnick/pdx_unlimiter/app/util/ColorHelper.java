package com.crschnick.pdx_unlimiter.app.util;

import com.crschnick.pdx_unlimiter.app.install.GameInstallation;
import com.crschnick.pdx_unlimiter.app.core.ErrorHandler;
import com.crschnick.pdx_unlimiter.core.info.SavegameInfo;
import com.crschnick.pdx_unlimiter.core.info.ck3.Ck3Tag;
import com.crschnick.pdx_unlimiter.core.info.stellaris.StellarisTag;
import com.crschnick.pdx_unlimiter.core.parser.Node;
import com.crschnick.pdx_unlimiter.core.parser.TextFormatParser;
import com.crschnick.pdx_unlimiter.core.parser.ValueNode;
import javafx.scene.paint.Color;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ColorHelper {

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

    public static javafx.scene.paint.Color colorFromInt(int c, int alpha) {
        return Color.rgb(c >>> 24, (c >>> 16) & 255, (c >>> 8) & 255, alpha / 255.0);
    }

    public static int intFromColor(Color c) {
        return (0xFF << 24) + ((int) (c.getRed() * 0xFF) << 16) + ((int) (c.getGreen() * 0xFF) << 8) + ((int) (c.getBlue() * 0xFF));
    }

    private static javafx.scene.paint.Color colorFromNodes(List<Node> c, boolean hsv) {
        if (hsv) {
            return Color.hsb(c.get(0).getDouble(), c.get(1).getDouble(), c.get(2).getDouble());
        } else {

            return Color.color(c.get(0).getDouble(), c.get(1).getDouble(), c.get(2).getDouble());
        }
    }

    private static Map<String, Color> loadPredefinedColors(List<Node> nodes) {
        Map<String, Color> map = new HashMap<>();
        for (Node n : nodes) {
            var kv = n.getKeyValueNode();
            Node data = kv.getNode();
            List<Node> color;

            boolean isHsv = false;

            var colorData = data.getNodeForKey("flag");
            if (colorData instanceof ValueNode) {
                if (colorData.getString().equals("hsv")) {
                    isHsv = true;
                }
                color = data.getNodeArray().get(3).getNodeArray();
            } else {
                color = colorData.getNodeArray();
            }

            map.put(kv.getKeyName(), colorFromNodes(color, isHsv));
        }
        return map;
    }

    private static Map<String, Color> loadPredefinedCk3Colors(List<Node> nodes) {
        Map<String, Color> map = new HashMap<>();
        for (Node n : nodes) {
            var kv = n.getKeyValueNode();
            Node data = kv.getNode();

            boolean isHsv = false;

            var colorData = data.getNodeForKey("values").getNodeArray();
            if (data.getNodeForKey("type").getString().equals("hsv")) {
                isHsv = true;
            }
            if (data.getNodeForKey("type").getString().equals("hsv360")) {
                isHsv = true;
                colorData = colorData.stream()
                        .map(v -> new ValueNode(false, String.valueOf(v.getDouble() / 360D)))
                        .collect(Collectors.toList());
            }

            map.put(kv.getKeyName(), colorFromNodes(colorData, isHsv));
        }
        return map;
    }

    public static Map<String, Color> loadCk3(SavegameInfo<Ck3Tag> info) {
        var file = CascadeDirectoryHelper.openFile(
                Path.of("common").resolve("named_colors").resolve("default_colors.txt"),
                info,
                GameInstallation.CK3).get();
        try {
            Node node = TextFormatParser.textFileParser().parse(file);
            ColorNodeTransformer.transform(node.getNodeForKey("colors"));
            return loadPredefinedCk3Colors(node.getNodeForKey("colors").getNodeArray());
        } catch (Exception ex) {
            ErrorHandler.handleException(ex);
            return Map.of();
        }
    }

    public static Map<String, Color> loadStellarisColors(SavegameInfo<StellarisTag> info) {
        var file = CascadeDirectoryHelper.openFile(
                Path.of("flags").resolve("colors.txt"), info, GameInstallation.STELLARIS).get();

        try {
            Node node = TextFormatParser.textFileParser().parse(file);
            return loadPredefinedColors(node.getNodeForKey("colors").getNodeArray());
        } catch (Exception ex) {
            ErrorHandler.handleException(ex);
            return Map.of();
        }
    }
}

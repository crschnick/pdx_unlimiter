package com.crschnick.pdx_unlimiter.app.util;

import com.crschnick.pdx_unlimiter.app.game.GameCampaignEntry;
import com.crschnick.pdx_unlimiter.app.game.GameInstallation;
import com.crschnick.pdx_unlimiter.app.installation.ErrorHandler;
import com.crschnick.pdx_unlimiter.core.data.Ck3Tag;
import com.crschnick.pdx_unlimiter.core.data.StellarisTag;
import com.crschnick.pdx_unlimiter.core.format.ColorTransformer;
import com.crschnick.pdx_unlimiter.core.parser.Node;
import com.crschnick.pdx_unlimiter.core.parser.TextFormatParser;
import com.crschnick.pdx_unlimiter.core.parser.ValueNode;
import com.crschnick.pdx_unlimiter.core.savegame.Ck3SavegameInfo;
import com.crschnick.pdx_unlimiter.core.savegame.StellarisSavegameInfo;
import javafx.scene.paint.Color;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ColorHelper {

    public static javafx.scene.paint.Color colorFromInt(int c, int alpha) {
        return Color.rgb(c >>> 24, (c >>> 16) & 255, (c >>> 8) & 255, alpha / 255.0);
    }

    public static int intFromColor(Color c) {
        return (0xFF << 24) + ((int) (c.getRed() * 0xFF) << 16) + ((int) (c.getGreen() * 0xFF) << 8) + ((int) (c.getBlue() * 0xFF));
    }

    private static javafx.scene.paint.Color colorFromNodes(List<Node> c, boolean hsv) {
        if (hsv) {
            return Color.hsb(Node.getDouble(c.get(0)), Node.getDouble(c.get(1)), Node.getDouble(c.get(2)));
        } else {

            return Color.color(Node.getDouble(c.get(0)), Node.getDouble(c.get(1)), Node.getDouble(c.get(2)));
        }
    }

    private static Map<String,Color> loadPredefinedColors(List<Node> nodes) {
        Map<String,Color> map = new HashMap<>();
        for (Node n : nodes) {
            var kv = Node.getKeyValueNode(n);
            Node data = kv.getNode();
            List<Node> color;

            boolean isHsv = false;

            var colorData = Node.getNodeForKey(data, "flag");
            if (colorData instanceof ValueNode) {
                if (Node.getString(colorData).equals("hsv")) {
                    isHsv = true;
                }
                color = Node.getNodeArray(Node.getNodeArray(data).get(3));
            } else {
                color = Node.getNodeArray(colorData);
            }

            map.put(kv.getKeyName(), colorFromNodes(color, isHsv));
        }
        return map;
    }

    private static Map<String,Color> loadPredefinedCk3Colors(List<Node> nodes) {
        Map<String,Color> map = new HashMap<>();
        for (Node n : nodes) {
            var kv = Node.getKeyValueNode(n);
            Node data = kv.getNode();

            boolean isHsv = false;

            var colorData = Node.getNodeArray(Node.getNodeForKey(data, "values"));
            if (Node.getString(Node.getNodeForKey(data, "type")).equals("hsv")) {
                isHsv = true;
            }
            if (Node.getString(Node.getNodeForKey(data, "type")).equals("hsv360")) {
                isHsv = true;
                colorData = colorData.stream()
                        .map(v -> new ValueNode(Node.getDouble(v) / 360D))
                        .collect(Collectors.toList());
            }

            map.put(kv.getKeyName(), colorFromNodes(colorData, isHsv));
        }
        return map;
    }

    public static Map<String,Color> loadCk3(GameCampaignEntry<Ck3Tag, Ck3SavegameInfo> e)  {
        try {
            InputStream in = CascadeDirectoryHelper.openFile(
                    Path.of("common").resolve("named_colors").resolve("default_colors.txt"), e, GameInstallation.CK3).get();
            Node node = TextFormatParser.textFileParser().parse(in).get();
            new ColorTransformer().transform(Node.getNodeForKey(node, "colors"));
            return loadPredefinedCk3Colors(Node.getNodeArray(Node.getNodeForKey(node, "colors")));
        } catch (Exception ex) {
            ErrorHandler.handleException(ex);
            return Map.of();
        }
    }

    public static Map<String,Color> loadStellarisColors(GameCampaignEntry<StellarisTag, StellarisSavegameInfo> e)  {
        try {
            InputStream in = CascadeDirectoryHelper.openFile(Path.of("flags").resolve("colors.txt"), e, GameInstallation.STELLARIS).get();
            Node node = TextFormatParser.textFileParser().parse(in).get();
            return loadPredefinedColors(Node.getNodeArray(Node.getNodeForKey(node, "colors")));
        } catch (Exception ex) {
            ErrorHandler.handleException(ex);
            return Map.of();
        }
    }
}

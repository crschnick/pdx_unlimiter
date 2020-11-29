package com.crschnick.pdx_unlimiter.app.util;

import com.crschnick.pdx_unlimiter.eu4.parser.Node;
import com.crschnick.pdx_unlimiter.eu4.parser.TextFormatParser;
import com.crschnick.pdx_unlimiter.eu4.parser.ValueNode;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ColorHelper {

    public static javafx.scene.paint.Color colorFromInt(int c, int alpha) {
        return Color.rgb(c >>> 24, (c >>> 16) & 255, (c >>> 8) & 255, alpha / 255.0);
    }

    private static javafx.scene.paint.Color colorFromNodes(List<Node> c, boolean hsv) {
        if (hsv) {
            return Color.hsb(Node.getDouble(c.get(0)), Node.getDouble(c.get(1)), Node.getDouble(c.get(2)));
        } else {

            return Color.rgb(Node.getInteger(c.get(0)), Node.getInteger(c.get(1)), Node.getInteger(c.get(2)));
        }
    }

    private static Map<String,Color> loadColor(List<Node> nodes) throws IOException {
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

    public static Map<String,Color> loadStellarisColors(Path path) throws IOException {
        Node node = TextFormatParser.textFileParser().parse(
                Files.newInputStream(path)).get();
        return loadColor(Node.getNodeArray(Node.getNodeForKey(node, "colors")));
    }
}

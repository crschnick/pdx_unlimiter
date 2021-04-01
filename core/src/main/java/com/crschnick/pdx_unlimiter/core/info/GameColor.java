package com.crschnick.pdx_unlimiter.core.info;

import com.crschnick.pdx_unlimiter.core.node.ColorNode;
import com.crschnick.pdx_unlimiter.core.node.Node;
import com.crschnick.pdx_unlimiter.core.node.NodeContext;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class GameColor {

    public static GameColor fromRgbArray(Node node) {
        return new GameColor(Type.RGB, node.getNodeArray().stream()
                .map(Node::getString)
                .collect(Collectors.toList()));
    }

    public static GameColor fromColorNode(Node n) {
        return new GameColor(n.getColorNode().getColorType(), n.getColorNode().getValues().stream()
                .map(Node::getString)
                .collect(Collectors.toList()));
    }

    public static Type getColorType(NodeContext ctx, int index) {
        var begin = ctx.getLiteralsBegin()[index];
        var length = ctx.getLiteralsLength()[index];
        if (length != 3 && length != 6) {
            return null;
        }

        if (ctx.getData()[begin] != 'r' && ctx.getData()[begin] != 'h') {
            return null;
        }

        var end = begin + length;
        for (var t : Type.values()) {
            if (Arrays.equals(t.getBytes(), 0, t.id.length(), ctx.getData(), begin, end)) {
                return t;
            }
        }
        return null;
    }

    public enum Type {
        RGB("rgb", 3),
        HSV("hsv", 3),
        HSV360("hsv360", 3),
        HEX("hex", 1);

        private final String id;
        private final int components;
        private final byte[] bytes;

        Type(String id, int components) {
            this.id = id;
            this.components = components;
            this.bytes = id.getBytes(StandardCharsets.UTF_8);
        }

        public String getId() {
            return id;
        }

        public int getComponents() {
            return components;
        }

        public byte[] getBytes() {
            return bytes;
        }
    }

    private Type type;
    private List<String> values;

    public GameColor() {
    }

    public GameColor(Type type, List<String> values) {
        this.type = type;
        this.values = values;
    }

    public Type getType() {
        return type;
    }

    public List<String> getValues() {
        return values;
    }
}

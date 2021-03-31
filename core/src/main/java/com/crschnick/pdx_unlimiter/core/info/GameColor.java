package com.crschnick.pdx_unlimiter.core.info;

import com.crschnick.pdx_unlimiter.core.node.ColorNode;
import com.crschnick.pdx_unlimiter.core.node.Node;

import java.util.List;
import java.util.stream.Collectors;

public final class GameColor {

    public static GameColor fromRgbArray(Node node) {
        return new GameColor(Type.RGB, node.getNodeArray().stream()
                .map(Node::getString)
                .collect(Collectors.toList()));
    }

    public static GameColor fromColorNode(Node n) {
        Type t = switch (n.getColorNode().getColorName()) {
            case "rgb" -> Type.RGB;
            case "hsv" -> Type.HSV;
            case "hsv360" -> Type.HSV360;
            case "hex" -> Type.HEX;
            default -> throw new IllegalArgumentException("Invalid color type " + n.getColorNode());
        };

        return new GameColor(t, n.getColorNode().getValues().stream()
                .map(Node::getString)
                .collect(Collectors.toList()));
    }

    public enum Type {
        RGB,
        HSV,
        HSV360,
        HEX
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

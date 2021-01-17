package com.crschnick.pdx_unlimiter.core.parser;

public class TextFormatWriter {

    public static String write(Node node) {
        StringBuilder s = new StringBuilder();
        if (node instanceof ArrayNode) {
            node.getNodeArray().forEach(c -> s.append(node(0, c)).append("\n"));
        }
        return s.toString();
    }

    private static String node(int indent, Node n) {
        if (n instanceof KeyValueNode) {
            return n.getKeyValueNode().getKeyName() + "=" +
                    node(indent, n.getKeyValueNode().getNode());
        }

        if (n instanceof ArrayNode) {
            StringBuilder s = new StringBuilder();
            s.append("{\n");
            n.getNodeArray().forEach(c ->
                    s.append(" ".repeat(indent + 2)).append(node(indent + 2, c)).append("\n"));
            s.append(" ".repeat(indent)).append("}");
            return s.toString();
        }

        if (n instanceof ValueNode) {
            ValueNode val = (ValueNode) n;
            if (val.isStringValue()) {
                return "\"" + val.getValue() + "\"";
            } else {
                return val.getValue();
            }
        }

        throw new IllegalArgumentException();
    }
}

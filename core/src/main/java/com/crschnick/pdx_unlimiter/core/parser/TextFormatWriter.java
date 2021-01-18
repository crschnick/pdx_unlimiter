package com.crschnick.pdx_unlimiter.core.parser;

public class TextFormatWriter {

    private static class LimitedStringBuilder {
        private int currentLines = 0;
        private int maxLines;
        private StringBuilder sb;

        public LimitedStringBuilder(int maxLines) {
            this.maxLines = maxLines;
            this.sb = new StringBuilder();
        }

        private String value() {
            return sb.toString();
        }

        private void append(String s) {
            sb.append(s);
        }

        private boolean appendLine(String line) {
            sb.append(line).append("\n");
            currentLines++;
            return currentLines < maxLines;
        }
    }

    public static String write(Node node, int maxLines) {
        var sb = new LimitedStringBuilder(maxLines);
        boolean hitMaxLines = false;
        if (node instanceof ArrayNode) {
            for (var c : node.getNodeArray()) {
                if (!node(0, c, sb) || !sb.appendLine("")) {
                    hitMaxLines = true;
                    break;
                }
            }
        } else {
            hitMaxLines = !node(0, node, sb);
        }
        if (hitMaxLines) {
            sb.append("...");
        }
        return sb.value();
    }

    private static boolean node(int indent, Node n, LimitedStringBuilder sb) {
        if (n instanceof KeyValueNode) {
            sb.append(n.getKeyValueNode().getKeyName() + "=");
            if (!node(indent, n.getKeyValueNode().getNode(), sb)) {
                return false;
            }
        }

        if (n instanceof ArrayNode) {
            if (!sb.appendLine("{")) {
                return false;
            }
            for (var c : n.getNodeArray()) {
                sb.append(" ".repeat(indent + 2));
                if (!node(indent + 2, c, sb)) {
                    return false;
                }
                if (!sb.appendLine("")) {
                    return false;
                }

            }
            sb.append(" ".repeat(indent) + "}");
        }

        if (n instanceof ValueNode) {
            ValueNode val = (ValueNode) n;
            if (val.isStringValue()) {
                sb.append("\"" + val.getValue() + "\"");
            } else {
                sb.append(val.getValue());
            }
        }

        return true;
    }
}

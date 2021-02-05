package com.crschnick.pdx_unlimiter.core.parser;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

public class TextFormatWriter {

    private Charset charset;

    private TextFormatWriter(Charset charset) {
        this.charset = charset;
    }

    public static TextFormatWriter textFileWriter() {
        return new TextFormatWriter(StandardCharsets.UTF_8);
    }

    public static TextFormatWriter ck3SavegameWriter() {
        return new TextFormatWriter(StandardCharsets.UTF_8);
    }

    public static TextFormatWriter eu4SavegameWriter() {
        return new TextFormatWriter(StandardCharsets.ISO_8859_1);
    }

    public static TextFormatWriter stellarisSavegameWriter() {
        return new TextFormatWriter(StandardCharsets.UTF_8);
    }

    public static TextFormatWriter hoi4SavegameWriter() {
        return new TextFormatWriter(StandardCharsets.UTF_8);
    }

    public static String writeToString(Node node, int maxLines, String space) {
        var sb = new LimitedStringBuilder(maxLines, space);
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

        else if (n instanceof ArrayNode) {
            if (!sb.appendLine("{")) {
                return false;
            }
            for (var c : n.getNodeArray()) {
                sb.space(indent + 1);
                if (!node(indent + 1, c, sb)) {
                    return false;
                }
                if (!sb.appendLine("")) {
                    return false;
                }

            }
            sb.space(indent);
            sb.append("}");
        }

        else if (n instanceof ValueNode) {
            ValueNode val = (ValueNode) n;
            if (val.isStringValue()) {
                sb.append("\"" + val.getValue() + "\"");
            } else {
                sb.append(val.getValue());
            }
        }

        else if (n instanceof ColorNode) {
            ColorNode cn = (ColorNode) n;
            sb.append(cn.getColorName() + "{" + cn.getValues().stream().map(Node::getString)
                    .collect(Collectors.joining(" ")) + "}");
        }

        return true;
    }

    public void write(Node node, int maxLines, String space, Path out) throws IOException {
        Files.writeString(out, writeToString(node, maxLines, space), charset);
    }

    private static class LimitedStringBuilder {
        private int currentLines = 0;
        private int maxLines;
        private StringBuilder sb;
        private String space;

        public LimitedStringBuilder(int maxLines, String space) {
            this.maxLines = maxLines;
            this.sb = new StringBuilder();
            this.space = space;
        }

        private String value() {
            return sb.toString();
        }

        private void space(int nTimes) {
            sb.append(space.repeat(nTimes));
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
}

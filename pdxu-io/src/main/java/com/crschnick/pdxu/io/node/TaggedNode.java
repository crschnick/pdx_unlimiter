package com.crschnick.pdxu.io.node;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class TaggedNode extends Node {

    public static final TagType[] NO_TAGS = new TagType[0];
    public static final TagType[] COLORS = new TagType[] {TagType.RGB, TagType.HSV, TagType.HSV360, TagType.HEX};
    public static final TagType[] ALL = TagType.values();
    private TagType type;
    private List<ValueNode> values;

    public TaggedNode(TagType type, List<ValueNode> values) {
        this.type = type;
        this.values = values;
    }

    public static TagType getTagType(TagType[] possible, NodeContext ctx, int index) {
        if (possible.length == 0) {
            return null;
        }

        var begin = ctx.getLiteralBegin(index);
        var length = ctx.getLiteralLength(index);

        // Make lookup as fast as possible
        if (possible == COLORS) {
            if (length != 3 && length != 6) {
                return null;
            }

            var first = ctx.getData()[begin];
            if (first != 'r' && first != 'h' && first != 'R' && first != 'H') {
                return null;
            }

            var end = begin + length;
            for (var t : COLORS) {
                if (matches(t, ctx.getData(), begin, end)) {
                    return t;
                }
            }
        } else {
            if (length != 3 && length != 4 && length != 6) {
                return null;
            }

            var first = ctx.getData()[begin];
            if (first != 'r' && first != 'h' && first != 'R' && first != 'H' && first != 'l' && first != 'L') {
                return null;
            }

            var end = begin + length;
            for (var t : ALL) {
                if (matches(t, ctx.getData(), begin, end)) {
                    return t;
                }
            }
        }
        return null;
    }

    private static boolean matches(TagType t, byte[] data, int begin, int end) {
        var id = t.getBytes();
        if (id.length != end - begin) {
            return false;
        }

        for (int i = 0; i < id.length; i++) {
            var b = data[begin + i];
            if (b == id[i]) {
                continue;
            }

            if (t.isCaseSensitive()) {
                return false;
            }

            var dataChar = Character.toLowerCase((char) (b & 0xFF));
            var idChar = Character.toLowerCase((char) (id[i] & 0xFF));
            if (dataChar != idChar) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String getString() {
        return toString();
    }

    public void set(TaggedNode other) {
        this.type = other.getType();
        this.values = Collections.unmodifiableList(other.getValues());
    }

    @Override
    public String toString() {
        return type.getId() + " {" + values.stream().map(ValueNode::toString).collect(Collectors.joining(" ")) + "}";
    }

    public TagType getType() {
        return type;
    }

    public List<ValueNode> getValues() {
        return values;
    }

    @Override
    public String toDebugValue() {
        return toString();
    }

    @Override
    public Descriptor describe() {
        if (Arrays.stream(COLORS).anyMatch(t -> t.equals(this.type))) {
            return new Descriptor(ValueType.COLOR, KeyType.NONE);
        } else {
            return new Descriptor(values.getFirst().describe().getValueType(), KeyType.NONE);
        }
    }

    @Override
    public void write(NodeWriter writer) throws IOException {
        writer.write(type.getId());
        writer.write(" {");
        for (var v : values) {
            v.write(writer);
            writer.write(" ");
        }
        writer.write("}");
    }

    @Override
    public TaggedNode getTaggedNode() {
        return this;
    }

    @Override
    public boolean isValue() {
        return false;
    }

    @Override
    public boolean isArray() {
        return false;
    }

    @Override
    public boolean isTagged() {
        return true;
    }

    @Override
    public boolean matches(NodeMatcher matcher) {
        return matcher.matchesScalar(new NodeContext(type.getId(), false), 0);
    }

    @Override
    public Node copy() {
        return new TaggedNode(
                type,
                values.stream()
                        .map(valueNode -> valueNode.copy().getValueNode())
                        .toList());
    }

    public enum TagType {
        RGB("rgb", false),
        HSV("hsv", false),
        HSV360("hsv360", false),
        HEX("hex", false),
        LIST("list", true),
        UPPERCASE_LIST("LIST", true);

        private final String id;
        private final byte[] bytes;
        private final boolean caseSensitive;

        TagType(String id, boolean caseSensitive) {
            this.id = id;
            this.bytes = id.getBytes(StandardCharsets.UTF_8);
            this.caseSensitive = caseSensitive;
        }

        public boolean isCaseSensitive() {
            return caseSensitive;
        }

        public String getId() {
            return id;
        }

        public byte[] getBytes() {
            return bytes;
        }
    }
}

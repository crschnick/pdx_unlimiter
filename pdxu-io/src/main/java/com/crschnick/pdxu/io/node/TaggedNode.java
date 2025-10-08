package com.crschnick.pdxu.io.node;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class TaggedNode extends Node {

    public static final TagType[] NO_TAGS = new TagType[0];
    public static final TagType[] COLORS = new TagType[]{TagType.RGB, TagType.HSV, TagType.HSV360, TagType.HEX};
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

        var begin = ctx.getLiteralsBegin()[index];
        var length = ctx.getLiteralsLength()[index];

        // Make lookup as fast as possible
        if (possible == COLORS) {
            if (length != 3 && length != 6) {
                return null;
            }

            if (ctx.getData()[begin] != 'r' && ctx.getData()[begin] != 'h') {
                return null;
            }

            var end = begin + length;
            for (var t : COLORS) {
                if (Arrays.equals(t.getBytes(), 0, t.id.length(), ctx.getData(), begin, end)) {
                    return t;
                }
            }
        } else {
            if (length != 3 && length != 4 && length != 6) {
                return null;
            }

            if (ctx.getData()[begin] != 'r' && ctx.getData()[begin] != 'h' && ctx.getData()[begin] != 'l' && ctx.getData()[begin] != 'L') {
                return null;
            }

            var end = begin + length;
            for (var t : ALL) {
                if (Arrays.equals(t.getBytes(), 0, t.id.length(), ctx.getData(), begin, end)) {
                    return t;
                }
            }
        }
        return null;
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
        return type.getId() + " {" + values.stream()
                .map(ValueNode::toString)
                .collect(Collectors.joining(" ")) + "}";
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
        return new TaggedNode(type, values.stream().map(valueNode -> valueNode.copy().getValueNode()).toList());
    }

    public enum TagType {
        RGB("rgb"),
        HSV("hsv"),
        HSV360("hsv360"),
        HEX("hex"),
        LIST("list"),
        UPPERCASE_LIST("LIST");

        private final String id;
        private final byte[] bytes;

        TagType(String id) {
            this.id = id;
            this.bytes = id.getBytes(StandardCharsets.UTF_8);
        }

        public String getId() {
            return id;
        }

        public byte[] getBytes() {
            return bytes;
        }
    }
}

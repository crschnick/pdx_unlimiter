package com.crschnick.pdx_unlimiter.core.node;

import com.crschnick.pdx_unlimiter.core.info.GameColor;
import com.crschnick.pdx_unlimiter.core.parser.NodeWriter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

//TODO: Adapt to context instead of string
public final class ColorNode extends Node {

    private GameColor.Type colorType;
    private List<ValueNode> values;

    public ColorNode(GameColor.Type colorType, List<ValueNode> values) {
        this.colorType = colorType;
        this.values = values;
    }

    public void set(ColorNode other) {
        this.colorType = other.getColorType();
        this.values = Collections.unmodifiableList(other.getValues());
    }

    @Override
    public String toString() {
        return colorType.getId() + " {" + values.stream()
                .map(ValueNode::toString)
                .collect(Collectors.joining(" ")) + "}";
    }

    public GameColor.Type getColorType() {
        return colorType;
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
        return new Descriptor(ValueType.COLOR, KeyType.NONE);
    }

    @Override
    public void write(NodeWriter writer) throws IOException {
        writer.write(colorType.getId());
        writer.write(" {");
        for (var v : values) {
            v.write(writer);
            writer.write(" ");
        }
        writer.write("}");
    }

    @Override
    public ColorNode getColorNode() {
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
    public boolean isColor() {
        return true;
    }

    @Override
    public boolean matches(NodeMatcher matcher) {
        return matcher.matchesScalar(new NodeContext(colorType.getId(), false), 0);
    }
}

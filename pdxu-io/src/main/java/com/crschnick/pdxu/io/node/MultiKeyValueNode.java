package com.crschnick.pdxu.io.node;

import java.io.IOException;
import java.util.*;

public class MultiKeyValueNode extends Node {

    private final ArrayNode key;
    private final ArrayNode value;

    public MultiKeyValueNode(ArrayNode key, ArrayNode value) {
        this.key = Objects.requireNonNull(key);
        this.value = Objects.requireNonNull(value);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("MultiKeyValueNode(");
        sb.append(key);
        sb.append("=");
        sb.append(value);
        sb.append(")");
        return sb.toString();
    }

    @Override
    public String toDebugValue() {
        return NodeWriter.writeToString(key, Integer.MAX_VALUE, " ") + "="
                + NodeWriter.writeToString(value, Integer.MAX_VALUE, " ");
    }

    @Override
    public Descriptor describe() {
        return new Descriptor(value.describe().getValueType(), KeyType.ALL);
    }

    @Override
    public void write(NodeWriter writer) throws IOException {
        key.write(writer);
        writer.write("=");
        value.write(writer);
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
        return false;
    }

    @Override
    public boolean matches(NodeMatcher matcher) {
        return key.matches(matcher) || value.matches(matcher);
    }

    @Override
    public Node copy() {
        return new MultiKeyValueNode(key.copy(), value.copy());
    }
}

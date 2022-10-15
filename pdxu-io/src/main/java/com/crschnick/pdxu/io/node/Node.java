package com.crschnick.pdxu.io.node;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public abstract class Node {

    public abstract String toDebugValue();

    public abstract Descriptor describe();

    public final void forEach(BiConsumer<String, Node> c) {
        forEach(c, false);
    }

    public void forEach(BiConsumer<String, Node> c, boolean includeNullKeys) {
        throw new UnsupportedOperationException();
    }

    public boolean forEach(BiPredicate<String, Node> c, boolean includeNullKeys) {
        throw new UnsupportedOperationException();
    }

    public abstract void write(NodeWriter writer) throws IOException;

    public TaggedNode getTaggedNode() {
        throw new UnsupportedOperationException();
    }

    public ValueNode getValueNode() {
        throw new UnsupportedOperationException();
    }

    public ArrayNode getArrayNode() {
        throw new UnsupportedOperationException();
    }

    public List<Node> getNodeArray() {
        throw new UnsupportedOperationException();
    }

    public boolean getBoolean() {
        throw new UnsupportedOperationException();
    }

    public String getString() {
        throw new UnsupportedOperationException();
    }

    public int getInteger() {
        throw new UnsupportedOperationException();
    }

    public long getLong() {
        throw new UnsupportedOperationException();
    }

    public double getDouble() {
        throw new UnsupportedOperationException();
    }

    public abstract boolean isValue();

    public abstract boolean isArray();

    public abstract boolean isTagged();

    public abstract boolean matches(NodeMatcher matcher);

    public boolean hasKey(String key) {
        throw new UnsupportedOperationException();
    }

    public Node getNodeForKey(String key) {
        throw new UnsupportedOperationException();
    }

    public Node getNodeForKeys(String... keys) {
        var current = this;
        for (var key : keys) {
            current = current.getNodeForKeyIfExistent(key).orElse(null);
            if (current == null) {
                return null;
            }
        }
        return current;
    }

    public Optional<Node> getNodeForKeysIfExistent(String... keys) {
        return Optional.ofNullable(getNodeForKeys(keys));
    }

    public Optional<Node> getNodeForKeyIfExistent(String key) {
        throw new UnsupportedOperationException();
    }

    public abstract Node copy();

    public List<Node> getNodesForKey(String key) {
        throw new UnsupportedOperationException();
    }

    public enum KeyType {
        NONE,
        ALL,
        MIXED
    }

    public enum ValueType {
        TEXT,
        BOOLEAN,
        INTEGER,
        FLOATING_POINT,
        UNQUOTED_STRING,
        COLOR
    }

    public static class Descriptor {
        private final ValueType valueType;
        private final KeyType keyType;

        public Descriptor(ValueType valueType, KeyType keyType) {
            this.valueType = valueType;
            this.keyType = keyType;
        }

        public ValueType getValueType() {
            return valueType;
        }

        public KeyType getKeyType() {
            return keyType;
        }
    }
}

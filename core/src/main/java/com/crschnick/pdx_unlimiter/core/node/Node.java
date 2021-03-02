package com.crschnick.pdx_unlimiter.core.node;

import com.crschnick.pdx_unlimiter.core.parser.NodeWriter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

public abstract class Node {

    public abstract Descriptor describe();

    public final void forEach(BiConsumer<String, Node> c) {
        forEach(c, false);
    }

    public void forEach(BiConsumer<String, Node> c, boolean includeNullKeys) {
        throw new UnsupportedOperationException();
    }

    public abstract void write(NodeWriter writer) throws IOException;

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

    public abstract boolean isColor();

    public boolean hasKey(String key) {
        throw new UnsupportedOperationException();
    }

    public Node getNodeForKey(String key) {
        throw new UnsupportedOperationException();
    }

    public Optional<Node> getNodeForKeyIfExistent(String key) {
        throw new UnsupportedOperationException();
    }

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

package com.crschnick.pdx_unlimiter.core.node;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

public abstract class Node {

    public static enum ArrayType {
        NONE,
        ARRAY,
        OBJECT,
        MIXED
    }

    public static class Descriptor {
        private ValueNode.Type basicType;
        private ArrayType arrayType;

        public Descriptor(ValueNode.Type basicType, ArrayType arrayType) {
            this.basicType = basicType;
            this.arrayType = arrayType;
        }

        public ValueNode.Type getBasicType() {
            return basicType;
        }

        public ArrayType getArrayType() {
            return arrayType;
        }
    }

    public final void forEach(BiConsumer<String, Node> c) {
        forEach(c, false);
    }

    public void forEach(BiConsumer<String, Node> c, boolean includeNullKeys) {
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
}

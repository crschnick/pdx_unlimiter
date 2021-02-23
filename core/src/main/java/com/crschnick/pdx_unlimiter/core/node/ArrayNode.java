package com.crschnick.pdx_unlimiter.core.node;

import java.util.*;
import java.util.function.BiConsumer;

public class ArrayNode extends Node {

    public static class Builder {

        private int index;

        private final NodeContext context;
        private final int[] keysBegin;
        private final int[] keysLength;
        private final List<Node> values;

        public Builder(NodeContext context, int maxSize) {
            this.context = context;
            this.keysBegin = new int[maxSize];
            this.keysLength = new int[maxSize];
            this.values = new ArrayList<>(maxSize);
        }

        public Node build() {
            return new ArrayNode(context, keysBegin, keysLength, values);
        }

        public void put(Node value) {
            keysBegin[index] = -1;
            keysLength[index] = -1;
            values.add(value);
            index++;
        }

        public void put(int begin, int length, Node value) {
            keysBegin[index] = begin;
            keysLength[index] = length;
            values.add(value);
            index++;
        }

        public int getUsedSize() {
            return index;
        }
    }

    private final NodeContext context;
    private final int[] keysBegin;
    private final int[] keysLength;
    private final List<Node> values;

    public ArrayNode(NodeContext context, int[] keysBegin, int[] keysLength, List<Node> values) {
        this.context = context;
        this.keysBegin = keysBegin;
        this.keysLength = keysLength;
        this.values = values;
    }

    public void forEach(BiConsumer<String, Node> c, boolean includeNullKeys) {
        for (int i = 0; i < values.size(); i++) {
            if (!hasKeyAtIndex(i) && !includeNullKeys) {
                continue;
            }

            c.accept(context.evaluate(keysBegin[i], keysLength[i]), values.get(i));
        }
    }

    @Override
    public boolean isValue() {
        return false;
    }

    @Override
    public boolean isArray() {
        return true;
    }

    @Override
    public boolean isColor() {
        return false;
    }

    private boolean hasKeyAtIndex(int index) {
        return keysBegin[index] != -1;
    }

    private boolean isKeyAt(int index, byte[] b) {
        if (!hasKeyAtIndex(index)) {
            return false;
        }

        if (keysLength[index] != b.length) {
            return false;
        }

        int start = keysBegin[index];
        for (int i = 0; i < keysLength[i]; i++) {
            if (context.getData()[start + i] != b[i]) {
                return false;
            }
        }
        return true;
    }

    private Node getNodeForKeyInternal(String key) {
        var b = key.getBytes(context.getCharset());
        for (int i = 0; i < values.size(); i++) {
            if (isKeyAt(i, b)) {
                return values.get(i);
            }
        }

        return null;
    }

    @Override
    public Optional<Node> getNodeForKeyIfExistent(String key) {
        return Optional.ofNullable(getNodeForKeyInternal(key));
    }

    @Override
    public boolean hasKey(String key) {
        return getNodeForKey(key) != null;
    }

    @Override
    public Node getNodeForKey(String key) {
        var n = getNodeForKeyInternal(key);
        if (n != null) {
            return n;
        }

        throw new IllegalArgumentException("Invalid key " + key);
    }

    @Override
    public List<Node> getNodesForKey(String key) {
        var b = key.getBytes(context.getCharset());
        List<Node> found = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            if (isKeyAt(i, b)) {
                found.add(values.get(i));
            }
        }
        return found;
    }
}

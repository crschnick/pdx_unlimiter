package com.crschnick.pdxu.io.node;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class ArrayNode extends Node {


    public static ArrayNode array(List<Node> values) {
        // Defensive copy of values
        return new SimpleArrayNode(new NodeContext(), null, null, new ArrayList<>(values));
    }

    public static ArrayNode sameKeyArray(String key, List<Node> values) {
        var ctx = new NodeContext(key, false);
        var ki = new int[values.size()];
        Arrays.fill(ki, 0);
        // Defensive copy of values
        return new SimpleArrayNode(ctx, ki, null, new ArrayList<>(values));
    }

    public static ArrayNode singleKeyNode(String key, Node value) {
        var ctx = new NodeContext(key, false);
        return new SimpleArrayNode(ctx, new int[]{0}, new int[]{-1}, List.of(value));
    }

    public final ArrayNode replaceKey(String key, Node value) {
        var r = ArrayNode.singleKeyNode(key, value);
        for (int i = 0; i < size(); i++) {
            if (isKeyAt(key, i)) {
                return replacePart(r, i, 1);
            }
        }

        throw new IllegalArgumentException("Invalid key " + key);
    }

    public final ArrayNode replacePart(ArrayNode toInsert, int beginIndex, int length) {
        if (beginIndex == 0 && length == size()) {
            return toInsert;
        }

        // Splice at begin only
        if (beginIndex == 0) {
            return new LinkedArrayNode(List.of(toInsert, splice(length, size() - length)));
        }

        // Splice at end only
        if (beginIndex + length == size()) {
            return new LinkedArrayNode(List.of(splice(0, beginIndex), toInsert));
        }

        var begin = splice(0, beginIndex);
        var end = splice(beginIndex + length, size() - (beginIndex + length));
        return new LinkedArrayNode(List.of(begin, toInsert, end));
    }

    @Override
    public ArrayNode getArrayNode() {
        return this;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public abstract int size();

    public abstract boolean isKeyAt(String key, int index);

    public abstract ArrayNode splice(int begin, int length);

    protected abstract void writeInternal(NodeWriter writer) throws IOException;

    protected abstract void writeFlatInternal(NodeWriter writer) throws IOException;

    protected abstract boolean isFlat();

    public abstract String getKeyAt(int index);

    @Override
    public String toDebugValue() {
        return NodeWriter.writeToString(this, Integer.MAX_VALUE, " ");
    }

    public void writeTopLevel(NodeWriter writer) throws IOException {
        writeInternal(writer);
    }

    @Override
    public void write(NodeWriter writer) throws IOException {
        boolean flat = isFlat();
        if (flat) {
            writer.write("{");
            writeFlatInternal(writer);
            writer.space();
            writer.write("}");
            return;
        }

        writer.write("{");
        writer.newLine();
        writer.incrementIndent();

        writeInternal(writer);

        writer.decrementIndent();
        writer.indent();
        writer.write("}");
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
    public boolean isTagged() {
        return false;
    }

    public static class Builder {

        private final int maxSize;
        private final NodeContext context;
        private final int[] valueScalars;
        private final List<Node> values;
        private int index;
        private int[] keyScalars;

        public Builder(NodeContext context, int maxSize) {
            this.maxSize = maxSize;
            this.context = context;
            this.valueScalars = new int[maxSize];
            this.values = new ArrayList<>(maxSize);
        }

        private void initKeys() {
            if (keyScalars == null) {
                this.keyScalars = new int[maxSize];
                Arrays.fill(this.keyScalars, -1);
            }
        }

        public ArrayNode build() {
            return new SimpleArrayNode(context, keyScalars, valueScalars, values);
        }

        public void putScalarValue(int scalarIndex) {
            checkFull();

            valueScalars[index] = scalarIndex;
            values.add(null);
            index++;
        }

        public void putKeyAndScalarValue(int keyIndex, int scalarIndex) {
            checkFull();

            initKeys();
            keyScalars[index] = keyIndex;
            valueScalars[index] = scalarIndex;
            values.add(null);
            index++;
        }

        public void putNodeValue(Node node) {
            checkFull();

            valueScalars[index] = -1;
            values.add(node);
            index++;
        }

        public void putKeyAndNodeValue(int keyIndex, Node node) {
            checkFull();

            initKeys();
            keyScalars[index] = keyIndex;
            valueScalars[index] = -1;
            values.add(node);
            index++;
        }

        private void checkFull() {
            if (isFull()) {
                var string = build().toString();
                throw new IndexOutOfBoundsException("Node " + string + " is already full");
            }
        }

        public boolean isFull() {
            return getUsedSize() == getMaxSize();
        }

        public int getUsedSize() {
            return index;
        }

        public int getMaxSize() {
            return maxSize;
        }
    }
}

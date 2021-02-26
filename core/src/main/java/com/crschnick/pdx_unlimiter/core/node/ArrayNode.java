package com.crschnick.pdx_unlimiter.core.node;

import com.crschnick.pdx_unlimiter.core.parser.NodeWriter;

import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;

public final class ArrayNode extends Node {

    public static class Builder {

        private int index;

        private final int maxSize;
        private final NodeContext context;
        private int[] keyScalars;
        private final int[] valueScalars;
        private final List<Node> values;

        public Builder(NodeContext context, int maxSize) {
            this.maxSize = maxSize;
            this.context = context;
            this.valueScalars = new int[maxSize];
            this.values = new ArrayList<>(maxSize);
        }

        public Node build() {
            return new ArrayNode(context, keyScalars, valueScalars, values);
        }

        public void putScalarValue(int scalarIndex) {
            valueScalars[index] = scalarIndex;
            values.add(null);
            index++;
        }

        public void putKeyAndScalarValue(int keyIndex, int scalarIndex) {
            if (keyScalars == null) {
                this.keyScalars = new int[maxSize];
            }
            keyScalars[index] = keyIndex;
            valueScalars[index] = scalarIndex;
            values.add(null);
            index++;
        }

        public void putNodeValue(Node node) {
            valueScalars[index] = -1;
            values.add(node);
            index++;
        }

        public void putKeyAndNodeValue(int keyIndex, Node node) {
            if (keyScalars == null) {
                this.keyScalars = new int[maxSize];
            }
            keyScalars[index] = keyIndex;
            valueScalars[index] = -1;
            values.add(node);
            index++;
        }

        public int getUsedSize() {
            return index;
        }
    }

    public static ArrayNode array(List<Node> values) {
        return new ArrayNode(null, null, null, values);
    }

    public static ArrayNode singleKeyNode(String key, Node value) {
        var ctx = new NodeContext(key);
        return new ArrayNode(ctx, new int[]{0}, new int[] {-1}, List.of(value));
    }

    private final NodeContext context;
    private final int[] keyScalars;
    private final int[] valueScalars;
    private final List<Node> values;

    private ArrayNode(NodeContext context, int[] keyScalars, int[] valueScalars, List<Node> values) {
        this.context = context;
        this.keyScalars = keyScalars;
        this.valueScalars = valueScalars;
        this.values = values;
    }

    @Override
    public String toString() {
        return "{ (" + values.size() + ") }";
    }

    public int getSubsequentEqualKeyCount(int startIndex) {
        var b = context.getSubData(keyScalars[startIndex]);
        for (int i = startIndex + 1; i < values.size(); i++) {
            if (!isKeyAt(i, b)) {
                return i - startIndex - 1;
            }
        }
        return values.size() - startIndex - 1;
    }

    public ArrayNode splice(int begin, int length) {
        int[] ks = new int[length];
        int[] vs = new int[length];
        System.arraycopy(keyScalars, begin, ks, 0, length);
        System.arraycopy(valueScalars, begin, vs, 0, length);
        return new ArrayNode(context, ks, vs, values.subList(begin, begin + length));
    }

    private void evaluateAllValueNodes() {
        for (int i = 0; i < values.size(); i++) {
            if (values.get(i) == null) {
                values.set(i, new ValueNode(context, valueScalars[i]));
            }
        }
    }

    @Override
    public List<Node> getNodeArray() {
        evaluateAllValueNodes();
        return values;
    }

    @Override
    public Descriptor describe() {
        evaluateAllValueNodes();

        if (values.size() == 0) {
            // Empty array type
            return new Descriptor(null, KeyType.NONE);
        }

        boolean hasArrays = values.stream().anyMatch(Node::isArray);
        ValueType type = null;
        if (!hasArrays) {
            type = values.get(0).describe().getValueType();
            for (int i = 1; i < values.size(); i++) {
                var iT = values.get(i).describe().getValueType();
                if (!iT.equals(type)) {
                    type = null;
                    break;
                }
            }
        }

        int keyCount = 0;
        for (int i = 0; i < values.size(); i++) {
            if (hasKeyAtIndex(i)) {
                keyCount++;
            }
        }

        if (keyCount == 0) {
            return new Descriptor(type, KeyType.NONE);
        }
        if (keyCount == values.size()) {
            return new Descriptor(type, KeyType.ALL);
        }
        return new Descriptor(type, KeyType.MIXED);
    }

    public void forEach(BiConsumer<String, Node> c, boolean includeNullKeys) {
        evaluateAllValueNodes();
        String key = null;
        for (int i = 0; i < values.size(); i++) {
            if (!hasKeyAtIndex(i)) {
                if (!includeNullKeys) {
                    continue;
                } else {
                    key = null;
                }
            } else {
                key = context.evaluate(keyScalars[i]);
            }

            c.accept(key, values.get(i));
        }
    }

    @Override
    public void write(NodeWriter writer) throws IOException {
        //TODO: Can be done better
        evaluateAllValueNodes();

        writer.write("{");
        writer.newLine();
        writer.incrementIndent();

        for (int i = 0; i < values.size(); i++) {
            if (hasKeyAtIndex(i)) {
                writer.write(context, keyScalars[i]);
                writer.write("=");
            }

            values.get(i).write(writer);
            writer.newLine();
        }

        writer.decrementIndent();
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
    public boolean isColor() {
        return false;
    }

    private boolean hasKeyAtIndex(int index) {
        if (keyScalars == null) {
            return false;
        }

        return keyScalars[index] != -1;
    }

    private boolean isKeyAt(int index, byte[] b) {
        if (!hasKeyAtIndex(index)) {
            return false;
        }

        int keyScalarIndex = keyScalars[index];
        if (context.getLiteralsLength()[keyScalarIndex] != b.length) {
            return false;
        }

        int start = context.getLiteralsBegin()[keyScalarIndex];
        for (int i = 0; i < context.getLiteralsLength()[keyScalarIndex]; i++) {
            if (context.getData()[start + i] != b[i]) {
                return false;
            }
        }
        return true;
    }

    private Node getNodeForKeyInternal(String key) {
        // Check if this node has no keys
        if (keyScalars == null) {
            return null;
        }

        var b = key.getBytes(context.getCharset());
        for (int i = 0; i < values.size(); i++) {
            if (isKeyAt(i, b)) {
                // Initialize value node if we haven't done that already
                if (values.get(i) == null) {
                    values.set(i, new ValueNode(context, valueScalars[i]));
                }

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
        return getNodeForKeyInternal(key) != null;
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

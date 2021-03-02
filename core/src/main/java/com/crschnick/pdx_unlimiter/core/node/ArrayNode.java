package com.crschnick.pdx_unlimiter.core.node;

import com.crschnick.pdx_unlimiter.core.parser.NodeWriter;
import com.crschnick.pdx_unlimiter.core.parser.NodeWriterImpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

public abstract class ArrayNode extends Node {

    public static ArrayNode array(List<Node> values) {
        return new SimpleArrayNode(null, null, null, values);
    }

    public static ArrayNode singleKeyNode(String key, Node value) {
        var ctx = new NodeContext(key);
        return new SimpleArrayNode(ctx, new int[]{0}, new int[]{-1}, List.of(value));
    }

    public abstract boolean isKeyAt(String key, int index);

    public abstract ArrayNode splice(int begin, int length);

    protected abstract void writeInternal(NodeWriter writer) throws IOException;

    protected abstract void writeFlatInternal(NodeWriter writer) throws IOException;

    protected abstract boolean isFlat();

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
    public boolean isColor() {
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
            valueScalars[index] = scalarIndex;
            values.add(null);
            index++;
        }

        public void putKeyAndScalarValue(int keyIndex, int scalarIndex) {
            initKeys();
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
            initKeys();
            keyScalars[index] = keyIndex;
            valueScalars[index] = -1;
            values.add(node);
            index++;
        }

        public int getUsedSize() {
            return index;
        }
    }
}

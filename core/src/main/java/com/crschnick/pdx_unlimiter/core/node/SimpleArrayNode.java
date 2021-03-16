package com.crschnick.pdx_unlimiter.core.node;

import com.crschnick.pdx_unlimiter.core.parser.NodeWriter;

import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;

public final class SimpleArrayNode extends ArrayNode {

    private final NodeContext context;
    private final int[] keyScalars;
    private final int[] valueScalars;
    private final List<Node> values;

    SimpleArrayNode(NodeContext context, int[] keyScalars, int[] valueScalars, List<Node> values) {
        this.context = Objects.requireNonNull(context);
        this.keyScalars = keyScalars;
        this.valueScalars = valueScalars;
        this.values = Objects.requireNonNull(values);
    }

    @Override
    public String toString() {
        if (values.size() <= 10) {
            StringBuilder sb = new StringBuilder("SimpleArrayNode(");
            evaluateAllValueNodes();
            for (int i = 0; i < values.size(); i++) {
                if (hasKeyAtIndex(i)) {
                    sb.append(context.evaluate(keyScalars[i]));
                    sb.append("=");
                }
                sb.append(values.get(i).toString());
                sb.append(", ");
            }
            sb.delete(sb.length() - 2, sb.length());
            sb.append(")");
            return sb.toString();
        } else {
            return "SimpleArrayNode(" + values.size() + ")";
        }
    }

    @Override
    public int size() {
        return values.size();
    }

    @Override
    public boolean isKeyAt(String key, int index) {
        return isKeyAt(index, key.getBytes(context.getCharset()));
    }

    public ArrayNode splice(int begin, int length) {
        int[] ks = new int[length];
        int[] vs = new int[length];
        System.arraycopy(keyScalars, begin, ks, 0, length);
        System.arraycopy(valueScalars, begin, vs, 0, length);
        return new SimpleArrayNode(context, ks, vs, values.subList(begin, begin + length));
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
        return Collections.unmodifiableList(values);
    }

    @Override
    public boolean matches(NodeMatcher matcher) {
        for (int i = 0; i < values.size(); i++) {
            if (hasKeyAtIndex(i)) {
                if (matcher.matchesScalar(context, keyScalars[i])) {
                    return true;
                }
            }

            if (values.get(i) == null) {
                if (matcher.matchesScalar(context, valueScalars[i])) {
                    return true;
                }
            } else {
                if (values.get(i).matches(matcher)) {
                    return true;
                }
            }
        }
        return false;
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

        String key;
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

    protected void writeInternal(NodeWriter writer) throws IOException {
        for (int i = 0; i < values.size(); i++) {
            writer.indent();
            if (hasKeyAtIndex(i)) {
                writer.write(context, keyScalars[i]);
                writer.write("=");
            }

            if (values.get(i) == null) {
                writer.write(context, valueScalars[i]);
            } else {
                values.get(i).write(writer);
            }
            writer.newLine();
        }
    }

    @Override
    protected void writeFlatInternal(NodeWriter writer) throws IOException {
        for (int i = 0; i < values.size(); i++) {
            writer.space();
            writer.write(context, valueScalars[i]);
        }
    }

    @Override
    protected boolean isFlat() {
        if (keyScalars == null) {
            for (var v : values) {
                if (v != null && !v.isValue()) {
                    return false;
                }
            }
            return true;
        }
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

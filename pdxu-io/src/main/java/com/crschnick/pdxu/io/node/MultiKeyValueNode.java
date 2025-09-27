package com.crschnick.pdxu.io.node;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

public class MultiKeyValueNode extends ArrayNode {

    private final NodeContext context;
    private final int[] keyScalars;
    private final Node value;

    public MultiKeyValueNode(NodeContext context, int[] keyScalars, Node value) {
        this.context = Objects.requireNonNull(context);
        this.keyScalars = keyScalars;
        this.value = Objects.requireNonNull(value);
    }

    @Override
    public String toString() {
        if (keyScalars.length == 0) {
            return "MultiKeyValueNode(0)";
        }

        var keys = new StringBuilder();
        for (int i = 0; i < keyScalars.length; i++) {
            keys.append(getKeyAt(i));
            if (i != keyScalars.length - 1) {
                keys.append(", ");
            }
        }

        if (value.getArrayNode().size() <= 10) {
            StringBuilder sb = new StringBuilder("MultiKeyValueNode(" + keys + ": ");
            sb.append(value);
            sb.append(")");
            return sb.toString();
        } else {
            return "MultiKeyValueNode(" + keys + ": " + value.getArrayNode().size() + "... )";
        }
    }


    @Override
    public int size() {
        return keyScalars.length;
    }

    @Override
    public boolean isKeyAt(String key, int index) {
        return isKeyAt(index, key.getBytes(context.getCharset()));
    }

    public ArrayNode splice(int begin, int length) {
        int[] ks = keyScalars != null ? new int[length] : null;
        if (keyScalars != null) {
            System.arraycopy(keyScalars, begin, ks, 0, length);
        }

        return new MultiKeyValueNode(context, ks, value);
    }

    @Override
    public List<Node> getNodeArray() {
        var repeated = new ArrayList<Node>();
        for (int i = 0; i < keyScalars.length; i++) {
            repeated.add(value);
        }
        return Collections.unmodifiableList(repeated);
    }

    @Override
    public boolean matches(NodeMatcher matcher) {
        if (value.matches(matcher)) {
            return true;
        }

        for (int i = 0; i < keyScalars.length; i++) {
            if (matcher.matchesScalar(context, keyScalars[i])) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Descriptor describe() {
        if (keyScalars.length == 0) {
            // Empty array type
            return new Descriptor(null, KeyType.NONE);
        }

        return new Descriptor(null, KeyType.ALL);
    }

    public void forEach(BiConsumer<String, Node> c, boolean includeNullKeys) {
        forEach((k, v) -> {
            c.accept(k, v);
            return true;
        }, includeNullKeys);
    }

    protected void writeInternal(NodeWriter writer) throws IOException {
        value.write(writer);
        writer.write("=");
        writer.write("{");
        for (int i = 0; i < keyScalars.length; i++) {
            writer.write(context, keyScalars[i]);
            if (i != keyScalars.length - 1) {
                writer.write(",");
            }
        }
        writer.write("}");
    }

    @Override
    public boolean forEach(BiPredicate<String, Node> c, boolean includeNullKeys) {
        for (int i = 0; i < keyScalars.length; i++) {
            var key = context.evaluate(keyScalars[i]);
            if (!c.test(key, value)) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void writeFlatInternal(NodeWriter writer) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected boolean isFlat() {
        return false;
    }

    @Override
    public String getKeyAt(int index) {
        return context.evaluate(keyScalars[index]);
    }

    private boolean isKeyAt(int index, byte[] b) {
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

    @Override
    public Optional<Node> getNodeForKeyIfExistent(String key) {
        return Optional.ofNullable(hasKey(key) ? value : null);
    }

    @Override
    public Node copy() {
        return new MultiKeyValueNode(context, keyScalars, value.copy());
    }

    @Override
    public boolean hasKey(String key) {
        var b = key.getBytes(context.getCharset());
        for (int i = 0; i < keyScalars.length; i++) {
            if (isKeyAt(i, b)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Node getNodeForKey(String key) {
        if (hasKey(key)) {
            return value;
        }

        throw new IllegalArgumentException("Invalid key " + key);
    }

    @Override
    public List<Node> getNodesForKey(String key) {
        var b = key.getBytes(context.getCharset());
        List<Node> found = new ArrayList<>();
        for (int i = 0; i < keyScalars.length; i++) {
            if (isKeyAt(i, b)) {
                found.add(value);
            }
        }
        return found;
    }

}

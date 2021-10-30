package com.crschnick.pdxu.editor.node;

import com.crschnick.pdxu.io.node.ArrayNode;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.node.NodeMatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

public abstract class EditorNode {

    protected final String keyName;
    private final int indexInParent;
    private final EditorNode parent;

    public EditorNode(EditorNode parent, String keyName, int indexInParent) {
        this.parent = parent;
        this.keyName = keyName;
        this.indexInParent = indexInParent;
    }

    public static Optional<EditorNode> fastEditorSimpleNodeSearch(EditorNode parent, ArrayNode ar, String key) {
        AtomicInteger parentIndex = new AtomicInteger();
        AtomicInteger index = new AtomicInteger();
        AtomicInteger previousKeyStart = new AtomicInteger();
        AtomicReference<EditorSimpleNode> found = new AtomicReference<>();
        ar.forEach((k, v) -> {
            if (k != null) {
                boolean endsHere = index.get() + 1 == ar.size() ||
                        !ar.isKeyAt(k, index.get() + 1);
                if (!endsHere) {
                    index.getAndIncrement();
                    return true;
                }

                int start = previousKeyStart.get();
                int end = index.get();
                boolean shouldCollect = end > start;
                if (shouldCollect) {
                    index.getAndIncrement();
                    parentIndex.getAndIncrement();
                }

                previousKeyStart.set(end + 1);

                if (shouldCollect) {
                    return true;
                }
            } else {
                previousKeyStart.incrementAndGet();
            }

            if (k != null && k.equals(key)) {
                found.set(new EditorSimpleNode(
                        parent,
                        k,
                        parentIndex.get(),
                        index.get()));
                return false;
            }

            parentIndex.getAndIncrement();
            index.getAndIncrement();
            return true;
        }, true);
        return Optional.ofNullable(found.get());
    }

    public static List<EditorNode> create(EditorNode parent, ArrayNode ar) {
        var result = new ArrayList<EditorNode>();
        AtomicInteger parentIndex = new AtomicInteger();
        AtomicInteger index = new AtomicInteger();
        AtomicInteger previousKeyStart = new AtomicInteger();
        ar.forEach((k, v) -> {
            if (k != null) {
                boolean endsHere = index.get() + 1 == ar.size() ||
                        !ar.isKeyAt(k, index.get() + 1);
                if (!endsHere) {
                    index.getAndIncrement();
                    return;
                }

                int start = previousKeyStart.get();
                int end = index.get();
                boolean shouldCollect = end > start;

                if (shouldCollect) {
                    result.add(new EditorCollectorNode(
                            parent,
                            k,
                            parentIndex.get(),
                            start,
                            end - start + 1));
                    index.getAndIncrement();
                    parentIndex.getAndIncrement();
                }

                previousKeyStart.set(end + 1);

                if (shouldCollect) {
                    return;
                }
            } else {
                previousKeyStart.incrementAndGet();
            }

            result.add(new EditorSimpleNode(
                    parent,
                    k,
                    parentIndex.get(),
                    index.get()));

            parentIndex.getAndIncrement();
            index.getAndIncrement();
        }, true);
        return result;
    }

    public abstract void updateNodeAtRawIndex(Node replacementValue, String toInsertKeyName, int index);

    public abstract boolean filterKey(Predicate<String> filter);

    public abstract boolean filterValue(NodeMatcher matcher);

    public final String getNavigationName() {
        return getKeyName().orElseGet(() -> getParent().getNavigationName() + "[" + indexInParent + "]");
    }

    public abstract String getNavigationNameAtRawIndex(int index);

    public abstract boolean isReal();

    public final EditorNode getParent() {
        return parent;
    }

    public abstract ArrayNode getContent();

    public abstract List<EditorNode> expand();

    public abstract ArrayNode toWritableNode();

    public abstract boolean isValid();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EditorNode that = (EditorNode) o;
        return indexInParent == that.indexInParent && Objects.equals(keyName, that.keyName) && Objects.equals(parent, that.parent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(keyName, indexInParent, parent);
    }

    public Optional<String> getKeyName() {
        return Optional.ofNullable(keyName);
    }

    public abstract int getRawSize();

    public abstract Node getNodeAtRawIndex(int index);

    public abstract List<Node> getNodesInRawRange(int index, int length);

    public int getIndexInParent() {
        return indexInParent;
    }
}

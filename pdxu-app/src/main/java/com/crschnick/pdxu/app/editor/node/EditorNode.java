package com.crschnick.pdxu.app.editor.node;

import com.crschnick.pdxu.io.node.ArrayNode;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.node.NodeMatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

public abstract class EditorNode {

    protected final String keyName;
    private final int parentIndex;
    private final EditorNode directParent;

    public EditorNode(EditorNode directParent, String keyName, int parentIndex) {
        this.directParent = directParent;
        this.keyName = keyName;
        this.parentIndex = parentIndex;
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

    public abstract void updateNodeAtIndex(Node replacementValue, String toInsertKeyName, int index);

    public abstract boolean filterKey(Predicate<String> filter);

    public abstract boolean filterValue(NodeMatcher matcher);

    public final String getNavigationName() {
        return getKeyName().orElseGet(() -> getParent().getNavigationName() + "[" + parentIndex + "]");
    }

    public abstract String getNavigationNameAtIndex(int index);

    public abstract boolean isReal();

    public final EditorNode getParent() {
        return directParent;
    }

    public abstract ArrayNode getContent();

    public abstract List<EditorNode> expand();

    public abstract ArrayNode toWritableNode();

    public final boolean isValid() {
        if (getParent() == null) {
            return true;
        }

        return getParent().isValid() && getParent().getNavigationNameAtIndex(parentIndex).equals(getNavigationName());
    }

    public Optional<String> getKeyName() {
        return Optional.ofNullable(keyName);
    }

    public abstract int getSize();

    public abstract Node getNodeAtIndex(int index);

    public abstract List<Node> getNodesInRange(int index, int length);

    public int getParentIndex() {
        return parentIndex;
    }
}

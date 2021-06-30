package com.crschnick.pdxu.app.editor;

import com.crschnick.pdxu.io.node.ArrayNode;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.node.NodeMatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
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

    public static List<EditorNode> create(EditorNode parent, ArrayNode ar) {
        var result = new ArrayList<EditorNode>();
        AtomicInteger parentIndex = new AtomicInteger();
        AtomicInteger index = new AtomicInteger();
        AtomicInteger previousKeyStart = new AtomicInteger();
        ar.forEach((k, v) -> {
            if (k != null) {
                boolean endsHere = index.get() + 1 == ar.getNodeArray().size() ||
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
                            new ArrayList<>(ar.getNodeArray().subList(start, end + 1))));
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
                    index.get(),
                    v));

            parentIndex.getAndIncrement();
            index.getAndIncrement();
        }, true);
        return result;
    }

    public abstract void updateNodeAtIndex(Node replacementValue, String toInsertKeyName, int index);

    public abstract void replacePart(ArrayNode toInsert, int beginIndex, int length);

    public abstract void delete();

    public abstract boolean filterKey(Predicate<String> filter);

    public abstract boolean filterValue(NodeMatcher matcher);

    public abstract String getDisplayKeyName();

    public abstract String getNavigationName();

    public abstract boolean isReal();

    public abstract EditorSimpleNode getRealParent();

    public abstract ArrayNode getContent();

    public abstract List<EditorNode> expand();

    public abstract ArrayNode toWritableNode();

    public abstract void update(ArrayNode newNode);

    public EditorNode getDirectParent() {
        return directParent;
    }

    public Optional<String> getKeyName() {
        return Optional.ofNullable(keyName);
    }

    public int getParentIndex() {
        return parentIndex;
    }
}

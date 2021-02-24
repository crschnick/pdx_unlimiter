package com.crschnick.pdx_unlimiter.app.editor;

import com.crschnick.pdx_unlimiter.core.node.ArrayNode;
import com.crschnick.pdx_unlimiter.core.node.Node;

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
        ar.forEach((k, v) -> {
            if (k != null) {
                int end = index.get() + ar.getSubsequentEqualKeyCount(index.get());

                if (end > index.get()) {
                    result.add(new CollectorNode(
                            parent,
                            k,
                            parentIndex.get(),
                            index.get(),
                            new ArrayList<>(ar.getNodeArray().subList(index.get(), end + 1))));

                    index.set(end + 1);
                    parentIndex.getAndIncrement();
                    return;
                }
            }

            result.add(new SimpleNode(
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

    public abstract void delete();

    public abstract boolean filterKey(Predicate<String> filter);

    public abstract boolean filterValue(Predicate<String> filter);

    public abstract String displayKeyName();

    public abstract String navigationName();

    public abstract boolean isReal();

    public abstract SimpleNode getRealParent();

    public abstract List<EditorNode> open();

    public abstract Node toWritableNode();

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

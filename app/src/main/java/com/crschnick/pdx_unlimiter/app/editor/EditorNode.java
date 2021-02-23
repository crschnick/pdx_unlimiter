package com.crschnick.pdx_unlimiter.app.editor;

import com.crschnick.pdx_unlimiter.core.node.ArrayNode;
import com.crschnick.pdx_unlimiter.core.parser.KeyValueNode;
import com.crschnick.pdx_unlimiter.core.node.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class EditorNode {

    protected String keyName;
    private int parentIndex;
    private EditorNode directParent;

    public EditorNode(EditorNode directParent, String keyName, int parentIndex) {
        this.directParent = directParent;
        this.keyName = keyName;
        this.parentIndex = parentIndex;
    }

    public static List<EditorNode> create(EditorNode parent, List<Node> nodes) {
        var result = new ArrayList<EditorNode>();
        int parentIndex = 0;
        for (int i = 0; i < nodes.size(); ) {
            var n = nodes.get(i);
            if (n instanceof KeyValueNode) {
                var k = n.getKeyValueNode().getKeyName();
                int end = i;
                while (end + 1 < nodes.size() &&
                        nodes.get(end + 1) instanceof KeyValueNode &&
                        nodes.get(end + 1).getKeyValueNode().getKeyName().equals(k)) {
                    end++;
                }

                if (end > i) {
                    result.add(new CollectorNode(
                            parent,
                            k,
                            parentIndex,
                            i,
                            nodes.subList(i, end + 1).stream()
                                    .map(node -> node.getKeyValueNode().getNode())
                                    .collect(Collectors.toList())));
                    i = end + 1;
                    parentIndex++;
                    continue;
                }
            }

            result.add(new SimpleNode(
                    parent,
                    n instanceof KeyValueNode ? n.getKeyValueNode().getKeyName() : null,
                    parentIndex,
                    i,
                    n instanceof KeyValueNode ? n.getKeyValueNode().getNode() : n));
            parentIndex++;
            i++;
        }

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

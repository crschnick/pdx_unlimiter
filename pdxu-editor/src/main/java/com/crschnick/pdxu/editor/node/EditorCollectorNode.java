package com.crschnick.pdxu.editor.node;

import com.crschnick.pdxu.io.node.ArrayNode;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.node.NodeMatcher;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public final class EditorCollectorNode extends EditorNode {

    private final int firstNodeIndex;
    private final int length;

    public EditorCollectorNode(EditorNode directParent, String keyName, int parentIndex, int firstNodeIndex, int length) {
        super(directParent, keyName, parentIndex);
        this.firstNodeIndex = firstNodeIndex;
        this.length = length;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        EditorCollectorNode that = (EditorCollectorNode) o;
        return firstNodeIndex == that.firstNodeIndex && length == that.length;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), firstNodeIndex, length);
    }

    @Override
    public boolean isValid() {
        if (!getParent().isValid()) {
            return false;
        }

        if (getParent().getRawSize() < firstNodeIndex + length) {
            return false;
        }

        boolean priorValid = firstNodeIndex == 0 ||
                !getParent().getNavigationNameAtRawIndex(firstNodeIndex - 1).equals(getNavigationName());
        if (!priorValid) {
            return false;
        }

        int endIndex = firstNodeIndex + length - 1;
        boolean posteriorValid = endIndex == getParent().getRawSize() - 1 ||
                !getParent().getNavigationNameAtRawIndex(endIndex + 1).equals(getNavigationName());
        if (!posteriorValid) {
            return false;
        }

        for (int i = firstNodeIndex; i < firstNodeIndex + length; i++) {
            var prk = getParent().getNavigationNameAtRawIndex(i);
            if (!prk.equals(getNavigationName())) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void updateNodeAtRawIndex(Node replacementValue, String toInsertKeyName, int index) {
        getParent().updateNodeAtRawIndex(replacementValue, keyName, firstNodeIndex + index);
    }

    @Override
    public boolean filterKey(Predicate<String> filter) {
        return filter.test(keyName);
    }

    @Override
    public boolean filterValue(NodeMatcher matcher) {
        return getNodes().stream().anyMatch(n -> n.matches(matcher));
    }

    @Override
    public String getNavigationNameAtRawIndex(int index) {
        return getNavigationName() + "[" + index + "]";
    }

    @Override
    public boolean isReal() {
        return false;
    }

    @Override
    public List<EditorNode> expand() {
        return EditorNode.create(this, ArrayNode.array(getNodes()));
    }

    public ArrayNode toWritableNode() {
        return ArrayNode.array(getNodes());
    }

    @Override
    public int getRawSize() {
        return length;
    }

    @Override
    public Node getNodeAtRawIndex(int index) {
        return getParent().getNodeAtRawIndex(firstNodeIndex + index);
    }

    @Override
    public List<Node> getNodesInRawRange(int index, int length) {
        return getParent().getNodesInRawRange(firstNodeIndex + index, length);
    }

    @Override
    public ArrayNode getContent() {
        return ArrayNode.sameKeyArray(keyName, getNodes());
    }

    public List<Node> getNodes() {
        return getParent().getNodesInRawRange(firstNodeIndex, length);
    }
}

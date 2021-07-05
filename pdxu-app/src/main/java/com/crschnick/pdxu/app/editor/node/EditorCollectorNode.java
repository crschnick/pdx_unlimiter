package com.crschnick.pdxu.app.editor.node;

import com.crschnick.pdxu.io.node.ArrayNode;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.node.NodeMatcher;

import java.util.List;
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
    public void updateNodeAtIndex(Node replacementValue, String toInsertKeyName, int index) {
        getParent().updateNodeAtIndex(replacementValue, keyName, firstNodeIndex + index);
    }

    @Override
    public void replacePart(ArrayNode toInsert, int beginIndex, int length) {
        getParent().replacePart(
                ArrayNode.sameKeyArray(keyName, toInsert.getNodeArray()),
                firstNodeIndex + beginIndex,
                length);
    }

    @Override
    public void delete() {
        getParent().replacePart(
                ArrayNode.emptyArray(),
                firstNodeIndex,
                getSize());
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
    public String getDisplayKeyName() {
        return keyName + "(s)";
    }

    @Override
    public String getNavigationName() {
        return keyName + "(s)";
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
    public void update(ArrayNode newNode) {
        getParent().replacePart(
                ArrayNode.sameKeyArray(keyName, newNode.getNodeArray()),
                firstNodeIndex,
                getSize());
    }

    @Override
    public int getSize() {
        return length;
    }

    @Override
    public Node getNodeAtIndex(int index) {
        return getParent().getNodeAtIndex(firstNodeIndex + index);
    }

    @Override
    public List<Node> getNodesInRange(int index, int length) {
        return getParent().getNodesInRange(firstNodeIndex + index, length);
    }

    @Override
    public ArrayNode getContent() {
        return ArrayNode.sameKeyArray(keyName, getNodes());
    }

    public List<Node> getNodes() {
        return getParent().getNodesInRange(firstNodeIndex, firstNodeIndex + length);
    }
}

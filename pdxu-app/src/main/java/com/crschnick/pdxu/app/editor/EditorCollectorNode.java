package com.crschnick.pdxu.app.editor;

import com.crschnick.pdxu.io.node.ArrayNode;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.node.NodeMatcher;

import java.util.List;
import java.util.function.Predicate;

public final class EditorCollectorNode extends EditorNode {

    private final int firstNodeIndex;
    private List<Node> nodes;

    public EditorCollectorNode(EditorNode directParent, String keyName, int parentIndex, int firstNodeIndex, List<Node> nodes) {
        super(directParent, keyName, parentIndex);
        this.firstNodeIndex = firstNodeIndex;
        this.nodes = nodes;
    }

    @Override
    public void updateNodeAtIndex(Node replacementValue, String toInsertKeyName, int index) {
        getRealParent().updateNodeAtIndex(replacementValue, keyName, firstNodeIndex + index);
    }

    @Override
    public void replacePart(ArrayNode toInsert, int beginIndex, int length) {
        getRealParent().replacePart(
                ArrayNode.sameKeyArray(keyName, toInsert.getNodeArray()),
                firstNodeIndex + beginIndex,
                length);
    }

    @Override
    public void delete() {
        getRealParent().replacePart(
                ArrayNode.emptyArray(),
                firstNodeIndex,
                nodes.size());
    }

    @Override
    public boolean filterKey(Predicate<String> filter) {
        return filter.test(keyName);
    }

    @Override
    public boolean filterValue(NodeMatcher matcher) {
        return nodes.stream().anyMatch(n -> n.matches(matcher));
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
    public EditorSimpleNode getRealParent() {
        return (EditorSimpleNode) getDirectParent();
    }

    @Override
    public List<EditorNode> expand() {
        return EditorNode.create(this, ArrayNode.array(nodes));
    }

    public ArrayNode toWritableNode() {
        return ArrayNode.array(nodes);
    }

    @Override
    public void update(ArrayNode newNode) {
        getRealParent().replacePart(
                ArrayNode.sameKeyArray(keyName, newNode.getNodeArray()),
                firstNodeIndex,
                nodes.size());
        this.nodes = newNode.getNodeArray();
    }

    @Override
    public int getSize() {
        return nodes.size();
    }

    @Override
    public ArrayNode getContent() {
        return ArrayNode.sameKeyArray(keyName, nodes);
    }

    public List<Node> getNodes() {
        return nodes;
    }
}

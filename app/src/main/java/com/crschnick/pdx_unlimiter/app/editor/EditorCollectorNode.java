package com.crschnick.pdx_unlimiter.app.editor;

import com.crschnick.pdx_unlimiter.core.node.ArrayNode;
import com.crschnick.pdx_unlimiter.core.node.Node;
import com.crschnick.pdx_unlimiter.core.node.NodeMatcher;

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
        return keyName;
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

    public List<Node> getNodes() {
        return nodes;
    }
}

package com.crschnick.pdx_unlimiter.app.editor;

import com.crschnick.pdx_unlimiter.core.node.ArrayNode;
import com.crschnick.pdx_unlimiter.core.node.Node;

import java.util.List;
import java.util.function.Predicate;

public class CollectorNode extends EditorNode {

    private final int firstNodeIndex;
    private final List<Node> nodes;

    public CollectorNode(EditorNode directParent, String keyName, int parentIndex, int firstNodeIndex, List<Node> nodes) {
        super(directParent, keyName, parentIndex);
        this.firstNodeIndex = firstNodeIndex;
        this.nodes = nodes;
    }

    @Override
    public void delete() {
        var ar = getRealParent().getBackingNode().getNodeArray();
        for (int i = 0; i < nodes.size(); i++) {
            ar.remove(firstNodeIndex);
        }
    }

    @Override
    public boolean filterKey(Predicate<String> filter) {
        return filter.test(keyName);
    }

    @Override
    public boolean filterValue(Predicate<String> filter) {
        return false;
    }

    @Override
    public String displayKeyName() {
        return keyName + "(s)";
    }

    @Override
    public String navigationName() {
        return keyName;
    }

    @Override
    public boolean isReal() {
        return false;
    }

    @Override
    public SimpleNode getRealParent() {
        return (SimpleNode) getDirectParent();
    }

    @Override
    public List<EditorNode> open() {
        return EditorNode.create(this, ArrayNode.array(nodes));
    }

    public ArrayNode toWritableNode() {
        return ArrayNode.array(nodes);
    }

    @Override
    public void update(ArrayNode newNode) {
        getRealParent().insertArray(newNode, firstNodeIndex, firstNodeIndex + nodes.size());
    }

    public List<Node> getNodes() {
        return nodes;
    }
}

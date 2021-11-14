package com.crschnick.pdxu.editor.node;

import com.crschnick.pdxu.io.node.ArrayNode;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.node.NodeMatcher;

import java.util.List;
import java.util.function.Predicate;

public abstract class EditorRealNode extends EditorNode {

    public EditorRealNode(EditorNode directParent, String keyName, int parentIndex) {
        super(directParent, keyName, parentIndex);
    }

    @Override
    public ArrayNode getContent() {
        return getKeyName().map(s -> ArrayNode.singleKeyNode(s, getBackingNode()))
                .orElse(ArrayNode.array(List.of(getBackingNode())));
    }

    @Override
    public boolean filterKey(Predicate<String> filter) {
        return filter.test(getNavigationName());
    }

    @Override
    public boolean filterValue(NodeMatcher matcher) {
        return this.getBackingNode().matches(matcher);
    }

    @Override
    public boolean isReal() {
        return true;
    }

    public abstract boolean isRoot();

    public abstract void update(ArrayNode newNode);

    @Override
    public List<EditorNode> expand() {
        return EditorNode.create(this, (ArrayNode) getBackingNode());
    }

    public ArrayNode toWritableNode() {
        return getBackingNode().isArray() ? (ArrayNode) getBackingNode() :
                ArrayNode.array(List.of(getBackingNode()));
    }

    @Override
    public int getRawSize() {
        return getBackingNode().getArrayNode().size();
    }

    public abstract Node getBackingNode();

    @Override
    public String getNavigationNameAtRawIndex(int index) {
        var s = getBackingNode().getArrayNode().getKeyAt(index);
        return s != null ? s : getNavigationName() + "[" + index + "]";
    }

    @Override
    public List<Node> getNodesInRawRange(int index, int length) {
        return getBackingNode().getNodeArray().subList(index, index + length);
    }
}

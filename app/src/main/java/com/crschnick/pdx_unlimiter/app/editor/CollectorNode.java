package com.crschnick.pdx_unlimiter.app.editor;

import com.crschnick.pdx_unlimiter.core.node.ArrayNode;
import com.crschnick.pdx_unlimiter.core.parser.KeyValueNode;
import com.crschnick.pdx_unlimiter.core.node.Node;
import com.crschnick.pdx_unlimiter.core.parser.TextFormatWriter;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CollectorNode extends EditorNode {

    private int firstNodeIndex;
    private List<Node> nodes;

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
        return nodes.stream().anyMatch(n -> filter.test(
                TextFormatWriter.writeToString(n, Integer.MAX_VALUE, "")));
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
        return EditorNode.create(this, nodes);
    }

    public Node toWritableNode() {
        return new ArrayNode(nodes);
    }

    @Override
    public void update(ArrayNode newNode) {
        var ar = getRealParent().getBackingNode().getNodeArray();

        for (int i = 0; i < nodes.size(); i++) {
            ar.remove(firstNodeIndex);
        }
        ar.addAll(firstNodeIndex, newNode.getNodeArray().stream()
                .map(node -> KeyValueNode.create(keyName, node)).collect(Collectors.toList()));
    }

    public List<Node> getNodes() {
        return nodes;
    }
}

package com.crschnick.pdx_unlimiter.app.editor;

import com.crschnick.pdx_unlimiter.core.parser.ArrayNode;
import com.crschnick.pdx_unlimiter.core.parser.KeyValueNode;
import com.crschnick.pdx_unlimiter.core.parser.Node;
import com.crschnick.pdx_unlimiter.core.parser.TextFormatWriter;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CollectorNode extends EditorNode {

    private List<Node> nodes;

    public CollectorNode(EditorNode parent, String keyName, List<Node> nodes) {
        super(parent, keyName);
        this.nodes = nodes;
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
        int firstIndex = ar.indexOf(ar.stream().filter(n -> n instanceof KeyValueNode &&
                n.getKeyValueNode().getKeyName().equals(keyName)).findFirst().get());
        ar.removeIf(n -> n instanceof KeyValueNode &&
                n.getKeyValueNode().getKeyName().equals(keyName));

        ar.addAll(firstIndex, newNode.getNodeArray().stream()
                .map(node -> KeyValueNode.create(keyName, node)).collect(Collectors.toList()));
    }

    public List<Node> getNodes() {
        return nodes;
    }
}

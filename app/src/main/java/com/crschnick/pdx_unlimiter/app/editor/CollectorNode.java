package com.crschnick.pdx_unlimiter.app.editor;

import com.crschnick.pdx_unlimiter.core.parser.ArrayNode;
import com.crschnick.pdx_unlimiter.core.parser.KeyValueNode;
import com.crschnick.pdx_unlimiter.core.parser.Node;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CollectorNode extends EditorNode {

    private List<Node> nodes;

    public CollectorNode(EditorNode parent, String keyName, List<Node> nodes) {
        super(parent, keyName);
        this.nodes = nodes;
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
        return new ArrayNode(nodes.stream()
                .map(node -> KeyValueNode.create(keyName, node))
                .collect(Collectors.toList()));
    }

    @Override
    public void update(ArrayNode newNode) {
        getRealParent().getBackingNode().getNodeArray()
                .removeIf(n -> n instanceof KeyValueNode &&
                        n.getKeyValueNode().getKeyName().equals(keyName));

        getRealParent().getBackingNode().getNodeArray().addAll(newNode.getNodeArray().stream()
                .map(node -> KeyValueNode.create(keyName, node)).collect(Collectors.toList()));
    }

    public List<Node> getNodes() {
        return nodes;
    }
}

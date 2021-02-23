package com.crschnick.pdx_unlimiter.core.node;

import com.crschnick.pdx_unlimiter.core.util.JoinedList;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public final class LinkedNode extends Node {

    private List<Node> arrayNodes;
    private JoinedList<Node> joined;

    public LinkedNode(List<Node> arrayNodes) {
        this.arrayNodes = arrayNodes;
        this.joined = new JoinedList<>(arrayNodes.stream()
                .map(Node::getNodeArray)
                .collect(Collectors.toList()));
    }

    @Override
    public void forEach(BiConsumer<String, Node> c, boolean includeNullKeys) {
        super.forEach(c, includeNullKeys);
    }

    @Override
    public List<Node> getNodeArray() {
        return joined;
    }

    @Override
    public boolean hasKey(String key) {
        return super.hasKey(key);
    }

    @Override
    public Node getNodeForKey(String key) {
        return super.getNodeForKey(key);
    }

    @Override
    public Optional<Node> getNodeForKeyIfExistent(String key) {
        return super.getNodeForKeyIfExistent(key);
    }

    @Override
    public List<Node> getNodesForKey(String key) {
        return super.getNodesForKey(key);
    }

    @Override
    public boolean isValue() {
        return false;
    }

    @Override
    public boolean isArray() {
        return true;
    }

    @Override
    public boolean isColor() {
        return false;
    }
}

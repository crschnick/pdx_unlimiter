package com.crschnick.pdx_unlimiter.core.node;

import com.crschnick.pdx_unlimiter.core.parser.NodeWriter;
import com.crschnick.pdx_unlimiter.core.util.JoinedList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public final class LinkedNode extends Node {

    private final List<Node> arrayNodes;
    private final JoinedList<Node> joined;

    public LinkedNode(List<Node> arrayNodes) {
        this.arrayNodes = arrayNodes;
        this.joined = new JoinedList<>(arrayNodes.stream()
                .map(Node::getNodeArray)
                .collect(Collectors.toList()));
    }

    @Override
    public Descriptor describe() {
        return arrayNodes.get(0).describe();
    }

    @Override
    public void forEach(BiConsumer<String, Node> c, boolean includeNullKeys) {
        super.forEach(c, includeNullKeys);
    }

    @Override
    public void write(NodeWriter writer) throws IOException {
        for (var n : joined) {
            n.write(writer);
            writer.newLine();
        }
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
        for (var ar : arrayNodes) {
            var r = ar.getNodeForKeyIfExistent(key);
            if (r.isPresent()) {
                return r.get();
            }
        }
        throw new IllegalArgumentException();
    }

    @Override
    public Optional<Node> getNodeForKeyIfExistent(String key) {
        for (var ar : arrayNodes) {
            var r = ar.getNodeForKeyIfExistent(key);
            if (r.isPresent()) {
                return r;
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Node> getNodesForKey(String key) {
        List<Node> found = new ArrayList<>();
        for (var ar : arrayNodes) {
            var r = ar.getNodeForKeyIfExistent(key);
            r.ifPresent(found::add);
        }
        return found;
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

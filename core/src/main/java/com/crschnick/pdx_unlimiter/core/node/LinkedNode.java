package com.crschnick.pdx_unlimiter.core.node;

import com.crschnick.pdx_unlimiter.core.parser.NodeWriter;
import com.crschnick.pdx_unlimiter.core.util.JoinedList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public final class LinkedNode extends ArrayNode {

    private final List<ArrayNode> arrayNodes;
    private final List<Node> joined;

    public LinkedNode(List<ArrayNode> arrayNodes) {
        this.arrayNodes = arrayNodes;
        this.joined = Collections.unmodifiableList(new JoinedList<>(arrayNodes.stream()
                .map(Node::getNodeArray)
                .collect(Collectors.toList())));
    }

    @Override
    public String toString() {
        return "{ (" + joined.size() + ") }";
    }

    @Override
    public Descriptor describe() {
        return arrayNodes.get(0).describe();
    }

    @Override
    public void forEach(BiConsumer<String, Node> c, boolean includeNullKeys) {
        for (var ar : arrayNodes) {
            ar.forEach(c, includeNullKeys);
        }
    }

    @Override
    public boolean isKeyAt(String key, int index) {
        return false;
    }

    @Override
    public ArrayNode splice(int begin, int length) {
        return null;
    }

    @Override
    protected void writeInternal(NodeWriter writer) throws IOException {
        for (var n : arrayNodes) {
            n.writeInternal(writer);
            writer.newLine();
        }
    }

    @Override
    protected void writeFlatInternal(NodeWriter writer) throws IOException {
        for (var n : arrayNodes) {
            n.writeFlatInternal(writer);
        }
    }

    @Override
    protected boolean isFlat() {
        return arrayNodes.stream().allMatch(ArrayNode::isFlat);
    }

    @Override
    public List<Node> getNodeArray() {
        return joined;
    }

    @Override
    public boolean matches(NodeMatcher matcher) {
        for (var n : arrayNodes) {
            if (n.matches(matcher)) {
                return true;
            }
        }
        return false;
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
}

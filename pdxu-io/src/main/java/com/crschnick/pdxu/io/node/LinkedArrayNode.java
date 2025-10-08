package com.crschnick.pdxu.io.node;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public final class LinkedArrayNode extends ArrayNode {

    private final List<ArrayNode> arrayNodes;
    private List<Node> joined;

    public LinkedArrayNode(List<ArrayNode> arrayNodes) {
        this.arrayNodes = new ArrayList<>(arrayNodes);
    }

    @Override
    public String toString() {
        return "LinkedArrayNode(" + getNodeArray().size() + ")";
    }

    @Override
    public Descriptor describe() {
        return arrayNodes.getFirst().describe();
    }

    @Override
    public void forEach(BiConsumer<String, Node> c, boolean includeNullKeys) {
        for (var ar : arrayNodes) {
            ar.forEach(c, includeNullKeys);
        }
    }

    @Override
    public int size() {
        return arrayNodes.stream().mapToInt(ArrayNode::size).sum();
    }

    @Override
    public boolean isKeyAt(String key, int index) {
        int list = getArrayNodeForIndex(index);
        return arrayNodes.get(list).isKeyAt(key, getLocalIndex(list, index));
    }

    @Override
    public ArrayNode splice(int begin, int length) {
        int ls = getArrayNodeForIndex(begin);
        // Use inclusive end index
        int le = getArrayNodeForIndex(begin + length - 1);

        // If only one sublist needs to be spliced, it can be done easily
        if (ls == le) {
            var b = getLocalIndex(ls, begin);
            var e = getLocalIndex(le, begin + length);
            return arrayNodes.get(ls).splice(b, e - b);
        }

        List<ArrayNode> spliced = new ArrayList<>();

        var localStartFirst = getLocalIndex(ls, begin);
        spliced.add(arrayNodes.get(ls).splice(localStartFirst, arrayNodes.get(ls).size() - localStartFirst));

        for (int i = ls + 1; i <= le - 1; i++) {
            spliced.add(arrayNodes.get(i));
        }

        var localEndLast = getLocalIndex(le, begin + length);
        spliced.add(arrayNodes.get(le).splice(0, localEndLast));
        return new LinkedArrayNode(spliced);
    }

    private int getLocalIndex(int listIndex, int absIndex) {
        int current = 0;
        for (int i = 0; i < listIndex; i++) {
            current += arrayNodes.get(i).size();
        }
        return absIndex - current;
    }

    private int getArrayNodeForIndex(int index) {
        int current = 0;
        for (var a : arrayNodes) {
            if (index < current + a.size()) {
                return arrayNodes.indexOf(a);
            } else {
                current += a.size();
            }
        }
        throw new IllegalArgumentException();
    }

    @Override
    protected void writeInternal(NodeWriter writer) throws IOException {
        for (var n : arrayNodes) {
            n.writeInternal(writer);
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
    public String getKeyAt(int index) {
        int list = getArrayNodeForIndex(index);
        return arrayNodes.get(list).getKeyAt(getLocalIndex(list, index));
    }

    @Override
    public List<Node> getNodeArray() {
        // Lazy initialize joined list
        if (joined == null) {
            this.joined = new ArrayList<>();
            for (var n : arrayNodes) {
                joined.addAll(n.getNodeArray());
            }
            this.joined = Collections.unmodifiableList(joined);
        }

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
        for (var ar : arrayNodes) {
            if (ar.hasKey(key)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Node getNodeForKey(String key) {
        for (var ar : arrayNodes) {
            var r = ar.getNodeForKeyIfExistent(key);
            if (r.isPresent()) {
                return r.get();
            }
        }
        throw new IllegalArgumentException("Invalid key " + key);
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
    public Node copy() {
        return new LinkedArrayNode(arrayNodes.stream().map(arrayNode -> arrayNode.copy().getArrayNode()).toList());
    }

    @Override
    public boolean forEach(BiPredicate<String, Node> c, boolean includeNullKeys) {
        for (var ar : arrayNodes) {
            if (!ar.forEach(c, includeNullKeys)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public List<Node> getNodesForKey(String key) {
        List<Node> found = new ArrayList<>();
        for (var ar : arrayNodes) {
            var r = ar.getNodesForKey(key);
            found.addAll(r);
        }
        return found;
    }
}

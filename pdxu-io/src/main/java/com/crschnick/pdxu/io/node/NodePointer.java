package com.crschnick.pdxu.io.node;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class NodePointer {

    public static interface Element {

        Node tryMatch(Node n);

        default String getKey() {
            return null;
        }
    }

    public static final record NameElement(String name) implements Element {

        @Override
        public Node tryMatch(Node n) {
            return n.getNodeForKeyIfExistent(name).orElse(null);
        }

        @Override
        public String getKey() {
            return name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static final record IndexElement(int index) implements Element {

        @Override
        public Node tryMatch(Node n) {
            if (n.getNodeArray().size() > index) {
                return n.getNodeArray().get(index);
            }
            return null;
        }

        @Override
        public String toString() {
            return "[" + index + "]";
        }
    }

    public static final record SupplierElement(Supplier<String> keySupplier) implements Element {

        @Override
        public Node tryMatch(Node n) {
            var name = keySupplier.get();
            if (name != null) {
                return n.getNodeForKeyIfExistent(name).orElse(null);
            }
            return null;
        }

        @Override
        public String getKey() {
            return keySupplier.get();
        }

        @Override
        public String toString() {
            return "[$s]";
        }
    }

    public static final record SelectorElement(Predicate<Node> selector) implements Element {

        @Override
        public Node tryMatch(Node n) {
            var res = n.getNodeArray().stream()
                    .filter(selector)
                    .findAny();
            return res.orElse(null);
        }

        @Override
        public String toString() {
            return "[$(...)]";
        }
    }


    public static class Builder {

        private final List<Element> path = new ArrayList<>();

        public Builder name(String name) {
            path.add(new NameElement(name));
            return this;
        }

        public Builder index(int index) {
            path.add(new IndexElement(index));
            return this;
        }

        public Builder selector(Predicate<Node> selector) {
            path.add(new SelectorElement(selector));
            return this;
        }

        public Builder pointer(Node context, NodePointer pointer) {
            return pointer(context, pointer, n -> n.getString());
        }

        public Builder pointer(Node context, NodePointer pointer, Function<Node, String> converter) {
            path.add(new SupplierElement(() -> {
                var res = pointer.get(context);
                if (res != null) {
                    return converter.apply(res);
                }
                return null;
            }));
            return this;
        }

        public NodePointer build() {
            return new NodePointer(path);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private final List<Element> path;

    public NodePointer(List<Element> path) {
        this.path = path;

        if (path.size() == 0) {
            throw new IllegalArgumentException();
        }
    }

    public String toString() {
        return "/" + path.stream().map(Element::toString).collect(Collectors.joining("/"));
    }

    public NodePointer sub(int begin, int end) {
        return new NodePointer(path.subList(begin, end));
    }

    public int size() {
        return path.size();
    }

    public boolean isValid(Node input) {
        return get(input) != null;
    }

    public Node get(Node input) {
        Node current = input;
        for (Element value : path) {
            var found = value.tryMatch(current);
            if (found == null) {
                return null;
            } else {
                current = found;
            }
        }
        return input;
    }


    public List<Element> getPath() {
        return path;
    }
}

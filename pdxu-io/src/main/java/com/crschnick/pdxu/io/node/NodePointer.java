package com.crschnick.pdxu.io.node;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class NodePointer {

    public static interface Element {

        Node tryMatch(Node root, Node n);

        default String getKey(Node root, Node n) {
            return null;
        }
    }

    public static final record NameElement(String name) implements Element {

        @Override
        public Node tryMatch(Node root, Node n) {
            return n.getNodeForKeyIfExistent(name).orElse(null);
        }

        @Override
        public String getKey(Node root, Node n) {
            return name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static final record IndexElement(int index) implements Element {

        @Override
        public Node tryMatch(Node root, Node n) {
            if (n.getNodeArray().size() > index && index >= 0) {
                return n.getNodeArray().get(index);
            }
            return null;
        }

        @Override
        public String getKey(Node root, Node n) {
            if (n.getNodeArray().size() > index && index >= 0) {
                return n.getArrayNode().getKeyAt(index);
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
        public Node tryMatch(Node root, Node n) {
            var name = keySupplier.get();
            if (name != null) {
                return n.getNodeForKeyIfExistent(name).orElse(null);
            }
            return null;
        }

        @Override
        public String getKey(Node root, Node n) {
            return keySupplier.get();
        }

        @Override
        public String toString() {
            return "[$s]";
        }
    }

    public static final record FunctionElement(BiFunction<Node, Node, String> keyFunc) implements Element {

        @Override
        public Node tryMatch(Node root, Node n) {
            var name = keyFunc.apply(root, n);
            if (name != null) {
                return n.getNodeForKeyIfExistent(name).orElse(null);
            }
            return null;
        }

        @Override
        public String getKey(Node root, Node n) {
            return keyFunc.apply(root, n);
        }

        @Override
        public String toString() {
            return "[$s]";
        }
    }

    public static final record SelectorElement(Predicate<Node> selector) implements Element {

        @Override
        public Node tryMatch(Node root, Node n) {
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

        private final List<Element> path;

        public Builder() {
            this.path = new ArrayList<>();
        }

        public Builder(NodePointer pointer) {
            this.path = new ArrayList<>(pointer.path);
        }


        public Builder name(String name) {
            path.add(new NameElement(name));
            return this;
        }

        public Builder index(int index) {
            path.add(new IndexElement(index));
            return this;
        }

        public Builder supplier(Supplier<String> keySupplier) {
            path.add(new SupplierElement(keySupplier));
            return this;
        }

        public Builder function(BiFunction<Node, Node, String> keyFunc) {
            path.add(new FunctionElement(keyFunc));
            return this;
        }

        public Builder selector(Predicate<Node> selector) {
            path.add(new SelectorElement(selector));
            return this;
        }

        public Builder pointerEvaluation(NodePointer pointer) {
            return pointerEvaluation(pointer, n -> {
                if (!n.isValue()) {
                    return null;
                }
                return n.getString();
            });
        }

        public Builder pointerEvaluation(NodePointer pointer, Function<Node, String> converter) {
            path.add(new FunctionElement((root, current) -> {
                var res = pointer.get(root);
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

    public static Builder fromBase(NodePointer pointer) {
        return new Builder(pointer);
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

    public Node get(Node root) {
        Node current = root;
        for (Element value : path) {
            var found = value.tryMatch(root, current);
            if (found == null) {
                return null;
            } else {
                current = found;
            }
        }
        return current;
    }

    public Optional<Node> getIfPresent(Node root) {
        return Optional.ofNullable(get(root));
    }

    public List<Element> getPath() {
        return path;
    }
}

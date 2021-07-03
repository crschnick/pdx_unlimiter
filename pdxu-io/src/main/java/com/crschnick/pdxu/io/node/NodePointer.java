package com.crschnick.pdxu.io.node;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class NodePointer {

    public static record Element(String name, int index, Predicate<Node> selector) {

        public String toString() {
            if (name != null) {
                return name;
            }
            if (index != -1) {
                return "[" + index + "]";
            }
            if (selector != null) {
                return "$[...]";
            }
            throw new IllegalArgumentException();
        }
    }

    public static class Builder {

        private final List<Element> path = new ArrayList<>();

        public Builder name(String name) {
            path.add(new Element(name, -1, null));
            return this;
        }

        public Builder index(int index) {
            path.add(new Element(null, index, null));
            return this;
        }

        public Builder selector(Predicate<Node> selector) {
            path.add(new Element(null, -1, selector));
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
        return get(input).isPresent();
    }

    public Optional<Node> get(Node input) {
        Node current = input;
        for (Element value : path) {
            var found = tryMatch(current, value);
            if (found.isEmpty()) {
                return Optional.empty();
            } else {
                current = found.get();
            }
        }
        return Optional.of(input);
    }

    private Optional<Node> tryMatch(Node current, Element e) {
        if (!current.isArray()) {
            return Optional.empty();
        }

        if (e.selector != null) {
            var selector = current.getNodeArray().stream()
                    .filter(e.selector)
                    .findAny();
            if (selector.isPresent()) {
                return selector;
            }
        }

        if (e.index != -1) {
            if (current.getNodeArray().size() > e.index) {
                return Optional.of(current.getNodeArray().get(e.index));
            }
        }

        if (e.name != null) {
            return current.getNodeForKeyIfExistent(e.name);
        }

        return Optional.empty();
    }
}

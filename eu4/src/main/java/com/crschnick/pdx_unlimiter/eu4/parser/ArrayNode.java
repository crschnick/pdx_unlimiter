package com.crschnick.pdx_unlimiter.eu4.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ArrayNode extends Node {

    private List<Node> nodes;

    public ArrayNode() {
        this.nodes = new ArrayList<>();
    }

    public ArrayNode(List<Node> nodes) {
        this.nodes = nodes;
    }

    public String toString(int indentation) {
        return "{\n" + getNodes().stream().map(
                (n) -> {
                    return indent(indentation + 1) + n.toString(indentation + 1);
                })
                .collect(Collectors.joining(",\n")) + "\n" + indent(indentation) + "}";
    }

    public void addNode(Node node) {
        nodes.add(node);
    }

    public void removeNode(Node node) {
        nodes.remove(node);
    }

    public List<Node> getNodes() {
        return nodes;
    }
}

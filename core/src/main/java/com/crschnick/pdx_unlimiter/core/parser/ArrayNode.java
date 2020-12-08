package com.crschnick.pdx_unlimiter.core.parser;

import java.util.ArrayList;
import java.util.List;

public class ArrayNode extends Node {

    private List<Node> nodes;

    public ArrayNode() {
        this.nodes = new ArrayList<>();
    }

    public ArrayNode(List<Node> nodes) {
        this.nodes = nodes;
    }

    public String toString() {
        return "ArrayNode(" + nodes.toString() + ")";
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

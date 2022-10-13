package com.crschnick.pdxu.io.node;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class NodeEnvironment {

    private final ArrayNode parent;
    private final Map<String, Node> variables;

    public NodeEnvironment(ArrayNode parent, Map<String, Node> variables) {
        this.parent = parent;
        this.variables = new HashMap<>(variables);
    }

    public NodeEnvironment put(String name, Node value) {
        variables.put(name, value);
        return this;
    }

    public NodeEnvironment copy(ArrayNode parent) {
        return new NodeEnvironment(parent, variables);
    }

    public Map<String, Node> getVariables() {
        return Collections.unmodifiableMap(variables);
    }

    public ArrayNode getParent() {
        return parent;
    }
}

package com.crschnick.pdxu.io.node;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class NodeEnvironment {

    private final Map<String, Node> variables;

    public NodeEnvironment(Map<String, Node> variables) {
        this.variables = new HashMap<>(variables);
    }

    public NodeEnvironment put(String name, Node value) {
        variables.put(name, value);
        return this;
    }

    public NodeEnvironment copy() {
        return new NodeEnvironment(variables);
    }

    public Map<String, Node> getVariables() {
        return Collections.unmodifiableMap(variables);
    }
}

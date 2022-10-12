package com.crschnick.pdxu.io.node;

import java.util.HashMap;
import java.util.Map;

public class NodeEnvironment {

    private final Map<String, Double> variables;

    public NodeEnvironment(Map<String, Double> variables) {
        this.variables = variables;
    }

    public NodeEnvironment copy() {
        return new NodeEnvironment(new HashMap<>(variables));
    }

    public Map<String, Double> getVariables() {
        return variables;
    }
}

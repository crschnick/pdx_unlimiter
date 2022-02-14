package com.crschnick.pdxu.io.savegame;

import com.crschnick.pdxu.io.node.ArrayNode;
import com.crschnick.pdxu.io.node.LinkedArrayNode;
import com.crschnick.pdxu.io.node.Node;

import java.util.Map;
import java.util.Set;

public final class SavegameContent {

    private final Map<String, ArrayNode> nodes;

    public SavegameContent(Map<String, ArrayNode> nodes) {
        this.nodes = nodes;
    }

    public Node combinedNode() {
        return new LinkedArrayNode(nodes.values().stream().toList());
    }

    public Set<Map.Entry<String, ArrayNode>> entrySet() {
        return nodes.entrySet();
    }

    public ArrayNode get() {
        return nodes.values().iterator().next();
    }

    public ArrayNode get(String name) {
        return nodes.get(name);
    }
}
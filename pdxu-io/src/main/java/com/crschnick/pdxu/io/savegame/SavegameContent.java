package com.crschnick.pdxu.io.savegame;

import com.crschnick.pdxu.io.node.ArrayNode;
import com.crschnick.pdxu.io.node.LinkedArrayNode;

import java.util.Map;
import java.util.Set;

public final class SavegameContent {

    private final Map<String, ArrayNode> nodes;
    private final ArrayNode combined;

    public SavegameContent(Map<String, ArrayNode> nodes) {
        this.nodes = nodes;
        this.combined = new LinkedArrayNode(nodes.values().stream().toList());
    }

    public ArrayNode combinedNode() {
        return combined;
    }

    public Set<Map.Entry<String, ArrayNode>> entrySet() {
        return nodes.entrySet();
    }

    public ArrayNode get() {
        return combinedNode();
    }

    public ArrayNode get(String name) {
        return nodes.get(name);
    }
}
package com.paradox_challenges.eu4_unlimiter.format;

import com.paradox_challenges.eu4_unlimiter.parser.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SubnodeTransformer extends NodeTransformer {

    private Map<String[], NodeTransformer> transformers;

    private boolean includeKey;

    public SubnodeTransformer(Map<String[], NodeTransformer> transformers, boolean includeKey) {
        this.transformers = transformers;
        this.includeKey = includeKey;
    }

    @Override
    public void transform(Node node) {
        for (Map.Entry<String[], NodeTransformer> entry : transformers.entrySet()) {
            List<Node> nodes = List.of(node);
            List<Node> newNodes = new ArrayList<>();
            for (String s : entry.getKey()) {
                for (Node current : nodes) {
                    newNodes.addAll(includeKey ? Node.getKeyValueNodesForKey(current ,s) : Node.getNodesForKey(current, s));
                }
                nodes = newNodes;
            }
            for (Node n : nodes) {
                entry.getValue().transform(n);
            }
        }

    }

    @Override
    public void reverse(Node node) {

    }
}

package com.paradox_challenges.eu4_unlimiter.format;

import com.paradox_challenges.eu4_unlimiter.parser.ArrayNode;
import com.paradox_challenges.eu4_unlimiter.parser.KeyValueNode;
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
            List<Node> nodes = new ArrayList<>();
            nodes.add(node);
            for (int i = 0; i < entry.getKey().length; i++) {
                String s = entry.getKey()[i];
                boolean isLast = i == entry.getKey().length - 1;
                List<Node> newNodes = new ArrayList<>();
                for (Node current : nodes) {
                    newNodes.addAll(isLast && includeKey ? Node.getKeyValueNodesForKey(current ,s) : Node.getNodesForKey(current, s));
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

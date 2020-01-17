package com.paradox_challenges.eu4_unlimiter.format;

import com.paradox_challenges.eu4_unlimiter.parser.ArrayNode;
import com.paradox_challenges.eu4_unlimiter.parser.KeyValueNode;
import com.paradox_challenges.eu4_unlimiter.parser.Node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NodeSplitter {

    private String[] names;

    public NodeSplitter(String... names) {
        this.names = names;
    }

    public Map<String, Node> removeNodes(Node node) {
        Map<String, Node> map = new HashMap<>();
        ArrayNode masterNode = (ArrayNode) node;
        for (String name : names) {
            KeyValueNode n = Node.getKeyValueNodeForKey(masterNode, name);
            masterNode.removeNode(n);
            map.put(name, new ArrayNode(List.of(n)));
        }
        return map;
    }
}

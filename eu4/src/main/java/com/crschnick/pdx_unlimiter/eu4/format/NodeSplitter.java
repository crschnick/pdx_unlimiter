package com.crschnick.pdx_unlimiter.eu4.format;

import com.crschnick.pdx_unlimiter.eu4.parser.ArrayNode;
import com.crschnick.pdx_unlimiter.eu4.parser.KeyValueNode;
import com.crschnick.pdx_unlimiter.eu4.parser.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NodeSplitter {

    private String[] names;

    public NodeSplitter(String... names) {
        this.names = names;
    }

    public Node splitFromNode(Node node) {
        List<Node> split = new ArrayList<>();
        ArrayNode masterNode = (ArrayNode) node;
        for (String name : names) {
            KeyValueNode n = Node.getKeyValueNodeForKey(masterNode, name);
            masterNode.removeNode(n);
            split.add(n);
        }
        return new ArrayNode(split);
    }

    public Map<String, Node> removeNodes(Node node) {
        Map<String, Node> map = new HashMap<>();
        ArrayNode masterNode = (ArrayNode) node;
        for (String name : names) {
            KeyValueNode n = Node.getKeyValueNodeForKey(masterNode, name);
            masterNode.removeNode(n);
            map.put(name, n.getNode());
        }
        return map;
    }
}

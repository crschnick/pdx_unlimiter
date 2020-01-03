package com.paradox_challenges.eu4_generator.format.eu4;

import com.paradox_challenges.eu4_generator.format.NodeTransformer;
import com.paradox_challenges.eu4_generator.savegame.ArrayNode;
import com.paradox_challenges.eu4_generator.savegame.KeyValueNode;
import com.paradox_challenges.eu4_generator.savegame.Node;

import java.util.ArrayList;
import java.util.List;

public class WarTransformer extends NodeTransformer {

    private NodeTransformer events = new EventTransformer();

    @Override
    public Node transformNode(Node node) {
        ArrayNode masterNode = (ArrayNode) node;
        List<KeyValueNode> warNodes = Node.getKeyValueNodesForKey(node, "war");
        List<Node> newWarNodes = new ArrayList<>();
        for (KeyValueNode warNode : warNodes) {
            masterNode.removeNode(warNode);
            events.transformNode(warNode.getNode());
            newWarNodes.add(warNode.getNode());
        }
        masterNode.addNode(new KeyValueNode("wars", new ArrayNode(newWarNodes)));
        return masterNode;
    }
}

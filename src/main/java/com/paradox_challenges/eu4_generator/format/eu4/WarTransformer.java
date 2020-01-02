package com.paradox_challenges.eu4_generator.format.eu4;

import com.paradox_challenges.eu4_generator.format.NodeTransformer;
import com.paradox_challenges.eu4_generator.savegame.ArrayNode;
import com.paradox_challenges.eu4_generator.savegame.KeyValueNode;
import com.paradox_challenges.eu4_generator.savegame.Node;

import java.util.List;

public class WarTransformer extends NodeTransformer {

    private NodeTransformer events = new EventTransformer();

    @Override
    public Node transformNode(Node node) {
        ArrayNode masterNode = (ArrayNode) node;
        List<Node> warNodes = getNodesForKey(node, "war");
        for (Node warNode : warNodes) {
            masterNode.removeNode(warNode);
            events.transformNode(getNodeForKey(warNode, "history"));
        }
        masterNode.addNode(new KeyValueNode("wars", new ArrayNode(warNodes)));
        return masterNode;
    }
}

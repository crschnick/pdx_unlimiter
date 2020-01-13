package com.paradox_challenges.eu4_generator.format.eu4;

import com.paradox_challenges.eu4_generator.format.CollectNodesTransformer;
import com.paradox_challenges.eu4_generator.format.NodeTransformer;
import com.paradox_challenges.eu4_generator.format.RenameKeyTransformer;
import com.paradox_challenges.eu4_generator.parser.Node;

public class Eu4Transformer extends NodeTransformer {
    @Override
    public Node transformNode(Node node) {
        new CollectNodesTransformer("ongoing_war", "ongoing_wars").transformNode(node);
        new CollectNodesTransformer("ended_war", "ended_wars").transformNode(node);
        new CollectNodesTransformer("rebels", "rebels").transformNode(node);
        new RenameKeyTransformer("trade_node", "trade_nodes").transformNode(node);
        //new CollectNodesTransformer("trade_node", "trade_nodes").transformNode(Node.getNodeForKey(node, "trade_nodes"));
        new ProvincesTransformer().transformNode(node);
        return node;
    }
}

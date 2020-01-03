package com.paradox_challenges.eu4_generator.format.eu4;

import com.paradox_challenges.eu4_generator.format.NodeTransformer;
import com.paradox_challenges.eu4_generator.savegame.ArrayNode;
import com.paradox_challenges.eu4_generator.savegame.Node;

public class Eu4Transformer extends NodeTransformer {
    @Override
    public Node transformNode(Node node) {
        new WarTransformer().transformNode(node);
        new ProvincesTransformer().transformNode(node);
        return node;
    }
}

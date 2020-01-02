package com.paradox_challenges.eu4_generator.format.eu4;

import com.paradox_challenges.eu4_generator.format.NodeTransformer;
import com.paradox_challenges.eu4_generator.savegame.ArrayNode;
import com.paradox_challenges.eu4_generator.savegame.KeyValueNode;
import com.paradox_challenges.eu4_generator.savegame.Node;
import com.paradox_challenges.eu4_generator.savegame.ValueNode;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ProvincesTransformer extends NodeTransformer {

    private NodeTransformer events = new EventTransformer();

    private static final Pattern PROVINCE_ID = Pattern.compile("-(\\d+)");

    @Override
    public Node transformNode(Node node) {
        ArrayNode masterNode = (ArrayNode) getNodeForKey(node, "provinces");
        for (Node sub : new ArrayList<>(masterNode.getNodes())) {
            KeyValueNode kv = (KeyValueNode) sub;
            var m = PROVINCE_ID.matcher(kv.getKeyName());
            m.find();
            String idString = m.group(1);
            int id = Integer.parseInt(idString);
            ((ArrayNode) kv.getNode()).addNode(KeyValueNode.create("province_id", new ValueNode<>(id)));

            events.transformNode(getNodeForKey(kv.getNode(), "history"));
            masterNode.removeNode(sub);
            masterNode.addNode(kv.getNode());
        }
        return masterNode;
    }
}

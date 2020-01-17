package com.paradox_challenges.eu4_unlimiter.format.eu4;

import com.paradox_challenges.eu4_unlimiter.format.NodeTransformer;
import com.paradox_challenges.eu4_unlimiter.parser.ArrayNode;
import com.paradox_challenges.eu4_unlimiter.parser.KeyValueNode;
import com.paradox_challenges.eu4_unlimiter.parser.Node;
import com.paradox_challenges.eu4_unlimiter.parser.ValueNode;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class ProvincesTransformer extends NodeTransformer {

    private NodeTransformer events = new EventTransformer();

    private static final Pattern PROVINCE_ID = Pattern.compile("-(\\d+)");

    @Override
    public Node transformNode(Node node) {
        ArrayNode masterNode = (ArrayNode) Node.getNodeForKey(node, "provinces");
        for (Node sub : new ArrayList<>(masterNode.getNodes())) {
            KeyValueNode kv = (KeyValueNode) sub;
            var m = PROVINCE_ID.matcher(kv.getKeyName());
            m.find();
            String idString = m.group(1);
            int id = Integer.parseInt(idString);
            ((ArrayNode) kv.getNode()).addNode(KeyValueNode.create("province_id", new ValueNode<>(id)));

            events.transformNode(kv.getNode());
            masterNode.removeNode(sub);
            masterNode.addNode(kv.getNode());
        }
        return masterNode;
    }
}

package com.paradox_challenges.eu4_unlimiter.format.eu4;

import com.paradox_challenges.eu4_unlimiter.format.NodeTransformer;
import com.paradox_challenges.eu4_unlimiter.parser.ArrayNode;
import com.paradox_challenges.eu4_unlimiter.parser.KeyValueNode;
import com.paradox_challenges.eu4_unlimiter.parser.Node;
import com.paradox_challenges.eu4_unlimiter.parser.ValueNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

public class ProvincesTransformer extends NodeTransformer {

    private NodeTransformer events = new EventTransformer();

    private static final Pattern PROVINCE_ID = Pattern.compile("-(\\d+)");

    private static final String ID = "province_id";

    @Override
    public void transform(Node node) {
        ArrayNode ar = (ArrayNode) node;
        for (int i = 0; i < ar.getNodes().size(); i++) {
            KeyValueNode kv = (KeyValueNode) ar.getNodes().get(i);
            var m = PROVINCE_ID.matcher(kv.getKeyName());
            m.find();
            String idString = m.group(1);
            int id = Integer.parseInt(idString);
            Node.addNodeToArray(kv.getNode(), KeyValueNode.create("province_id", new ValueNode<>(id)));
            ar.getNodes().set(i, kv.getNode());
            events.transform(kv.getNode());
        }
    }

    @Override
    public void reverse(Node node) {
        for (Node sub : Node.copyOfArrayNode(Node.getNodeForKey(node, "provinces"))) {
            ValueNode<Integer> provinceId = (ValueNode<Integer>) Node.getNodeForKey(sub, ID);
            Node.removeNodeFromArray(sub, provinceId);
            Node.removeNodeFromArray(node, sub);
            Node.addNodeToArray(node, KeyValueNode.create("-" + String.valueOf(provinceId.getValue()), sub));
        }
    }
}

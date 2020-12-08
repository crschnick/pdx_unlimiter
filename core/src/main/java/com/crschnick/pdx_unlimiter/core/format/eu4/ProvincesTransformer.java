package com.crschnick.pdx_unlimiter.core.format.eu4;

import com.crschnick.pdx_unlimiter.core.format.NodeTransformer;
import com.crschnick.pdx_unlimiter.core.parser.ArrayNode;
import com.crschnick.pdx_unlimiter.core.parser.KeyValueNode;
import com.crschnick.pdx_unlimiter.core.parser.Node;
import com.crschnick.pdx_unlimiter.core.parser.ValueNode;

import java.util.regex.Pattern;

public class ProvincesTransformer extends NodeTransformer {

    private static final Pattern PROVINCE_ID = Pattern.compile("-(\\d+)");
    private static final String ID = "province_id";
    private NodeTransformer events = new EventTransformer();

    @Override
    public void transform(Node node) {
        ArrayNode ar = (ArrayNode) node;
        for (int i = 0; i < ar.getNodes().size(); i++) {
            KeyValueNode kv = (KeyValueNode) ar.getNodes().get(i);
            var m = PROVINCE_ID.matcher(kv.getKeyName());
            m.find();
            String idString = m.group(1);
            long id = Long.parseLong(idString);
            Node.addNodeToArray(kv.getNode(), KeyValueNode.create("province_id", new ValueNode(id)));
            ar.getNodes().set(i, kv.getNode());
            events.transform(kv.getNode());
        }
    }

    @Override
    public void reverse(Node node) {
        for (Node sub : Node.copyOfArrayNode(Node.getNodeForKey(node, "provinces"))) {
            ValueNode provinceId = (ValueNode) Node.getNodeForKey(sub, ID);
            Node.removeNodeFromArray(sub, provinceId);
            Node.removeNodeFromArray(node, sub);
            Node.addNodeToArray(node, KeyValueNode.create("-" + String.valueOf(provinceId.getValue()), sub));
        }
    }
}

package com.crschnick.pdx_unlimiter.core.format.hoi4;

import com.crschnick.pdx_unlimiter.core.format.NodeTransformer;
import com.crschnick.pdx_unlimiter.core.parser.ArrayNode;
import com.crschnick.pdx_unlimiter.core.parser.KeyValueNode;
import com.crschnick.pdx_unlimiter.core.parser.Node;

public class CountryTransformer extends NodeTransformer {

    @Override
    public void transform(Node node) {
        KeyValueNode kv = Node.getKeyValueNode(node);
        KeyValueNode intel = (KeyValueNode) Node.getKeyValueNodeForKey(kv.getNode(), "intel");
        intel.setNode(new ArrayNode());
    }

    @Override
    public void reverse(Node node) {

    }
}

package com.crschnick.pdx_unlimiter.core.format.eu4;

import com.crschnick.pdx_unlimiter.core.data.GameDate;
import com.crschnick.pdx_unlimiter.core.data.GameDateType;
import com.crschnick.pdx_unlimiter.core.format.NodeTransformer;
import com.crschnick.pdx_unlimiter.core.parser.ArrayNode;
import com.crschnick.pdx_unlimiter.core.parser.KeyValueNode;
import com.crschnick.pdx_unlimiter.core.parser.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EventTransformer extends NodeTransformer {

    @Override
    public void transform(Node node) {
        Optional<Node> historyNode = Node.getNodeForKeyIfExistent(node, "history");
        if (!historyNode.isPresent()) {
            return;
        }

        ArrayNode history = (ArrayNode) historyNode.get();
        List<Node> newEventList = new ArrayList<>();
        for (Node n : new ArrayList<>(history.getNodes())) {
            //Remove empty group nodes (Important!)
            if (!(n instanceof KeyValueNode)) {
                history.getNodes().remove(n);
                continue;
            }
            KeyValueNode kv = (KeyValueNode) n;
            String date = kv.getKeyName();
            if (GameDateType.EU4.isDate(date)) {
                GameDate gd = GameDateType.EU4.fromString(date);
                Node dateNode = GameDateType.EU4.toNode(gd);
                ArrayNode an = (ArrayNode) kv.getNode();
                an.addNode(KeyValueNode.create("date", dateNode));
                newEventList.add(an);
                history.getNodes().remove(n);
            }
        }
        history.getNodes().add(KeyValueNode.create("events", new ArrayNode(newEventList)));
    }

    @Override
    public void reverse(Node node) {

    }
}

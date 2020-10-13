package com.crschnick.pdx_unlimiter.eu4.format.eu4;

import com.crschnick.pdx_unlimiter.eu4.format.NodeTransformer;
import com.crschnick.pdx_unlimiter.eu4.parser.GameDate;
import com.crschnick.pdx_unlimiter.eu4.parser.KeyValueNode;
import com.crschnick.pdx_unlimiter.eu4.parser.ArrayNode;
import com.crschnick.pdx_unlimiter.eu4.parser.Node;

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
            GameDate gd = GameDate.fromString(date);
            if (gd != null) {
                Node dateNode = GameDate.toNode(gd);
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

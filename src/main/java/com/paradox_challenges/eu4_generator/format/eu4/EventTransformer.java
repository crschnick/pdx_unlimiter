package com.paradox_challenges.eu4_generator.format.eu4;

import com.paradox_challenges.eu4_generator.format.DateTransformer;
import com.paradox_challenges.eu4_generator.format.NodeTransformer;
import com.paradox_challenges.eu4_generator.parser.ArrayNode;
import com.paradox_challenges.eu4_generator.parser.GameDate;
import com.paradox_challenges.eu4_generator.parser.KeyValueNode;
import com.paradox_challenges.eu4_generator.parser.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EventTransformer extends NodeTransformer {

    @Override
    public Node transformNode(Node node) {
        Optional<Node> historyNode = Node.getNodeForKeyIfExistent(node, "history");
        if (!historyNode.isPresent()) {
            return node;
        }

        ArrayNode history = (ArrayNode) historyNode.get();
        List<Node> newEventList = new ArrayList<>();
        for (Node n : new ArrayList<>(history.getNodes())) {
            KeyValueNode kv = (KeyValueNode) n;
            String date = kv.getKeyName();
            GameDate gd = GameDate.fromString(date);
            if (gd != null) {
                Node dateNode = DateTransformer.INSTANCE.toNode(gd);
                ArrayNode an = (ArrayNode) kv.getNode();
                an.addNode(KeyValueNode.create("date", dateNode));
                newEventList.add(an);
                history.getNodes().remove(n);
            }
        }
        history.getNodes().add(KeyValueNode.create("events", new ArrayNode(newEventList)));
        return node;
    }
}

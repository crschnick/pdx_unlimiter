package com.crschnick.pdx_unlimiter.eu4.format.eu4;

import com.crschnick.pdx_unlimiter.eu4.format.NodeTransformer;
import com.crschnick.pdx_unlimiter.eu4.parser.*;

import java.util.List;
import java.util.Optional;

public class CountryTransformer extends NodeTransformer {

    @Override
    public void transform(Node rootNode) {
        ArrayNode newHistory = new ArrayNode();
        Node.addNodeToArray(rootNode, KeyValueNode.create("countries_history", newHistory));
        ArrayNode ar = (ArrayNode) Node.getNodeForKey(rootNode, "countries");
        for (int i = 0; i < ar.getNodes().size(); i++) {
            KeyValueNode kv = (KeyValueNode) ar.getNodes().get(i);
            String tag = kv.getKeyName();
            Optional<Node> historyContent = Node.getNodeForKeyIfExistent(kv.getNode(), "history");
            if (historyContent.isPresent()) {
                Node.removeNodeFromArray(kv.getNode(), Node.getKeyValueNodeForKey(kv.getNode(), "history"));
                Node.addNodeToArray(newHistory, KeyValueNode.create(tag, historyContent.get()));
            }
        }

        // Collect initial values
        for (int i = 0; i < newHistory.getNodes().size(); i++) {
            KeyValueNode kv = (KeyValueNode) newHistory.getNodes().get(i);
            String tag = kv.getKeyName();
            List<Node> content = Node.getNodeArray(kv.getNode());
            Node initial = new ArrayNode();
            for (int j = 0; j < content.size(); j++) {
                KeyValueNode hi = Node.getKeyValueNode(content.get(j));
                if (!hi.getKeyName().equals("events")) {
                    content.remove(j);
                    Node.addNodeToArray(initial, hi);
                    j--;
                }
            }
            content.add(0, KeyValueNode.create("initial", initial));
        }

        // Remove empty event nodes
        for (int i = 0; i < newHistory.getNodes().size(); i++) {
            KeyValueNode kv = (KeyValueNode) newHistory.getNodes().get(i);
            Node events = Node.getNodeForKey(kv.getNode(), "events");
            List<Node> content = Node.getNodeArray(events);
            for (int j = 0; j < content.size(); j++) {
                if (Node.getNodeArray(content.get(j)).size() == 1) {
                    content.remove(j);
                    j--;
                }
            }
        }

        for (int i = 0; i < newHistory.getNodes().size(); i++) {
            KeyValueNode kv = (KeyValueNode) newHistory.getNodes().get(i);
            Node events = Node.getNodeForKey(kv.getNode(), "events");
            Node tagHistory = new ArrayNode();
            List<Node> content = Node.getNodeArray(events);
            for (int j = 0; j < content.size(); j++) {
                if (Node.hasKey(content.get(j), "changed_tag_from")) {
                    Node entry = new ArrayNode();
                    KeyValueNode changedFrom = Node.getKeyValueNodeForKey(content.get(j), "changed_tag_from");
                    KeyValueNode date = Node.getKeyValueNodeForKey(content.get(j), "date");
                    Node.addNodeToArray(entry, changedFrom);
                    Node.addNodeToArray(entry, date);

                    Node.addNodeToArray(tagHistory, entry);
                    content.remove(j);
                    j--;
                }
            }
            Node.addNodeToArray(kv.getNode(), KeyValueNode.create("tag_history", tagHistory));
        }

//
//        Node first = tagHistory.get(0);
//        String tag = Node.getString(Node.getNodeForKey(first, "changed_tag_from"));
//        GameDate date = GameDate.fromNode(Node.getNodeForKey(first, "changed_tag_from"));
//        List<Node> otherTagHis = Node.getNodeArray(Node.getNodeForKey(kv.getNode(), "tag_history"));
//        if (otherTagHis.size() == 0) {
//            continue;
//        }
//
//        for (int tagIndex = 0; tagIndex < otherTagHis.size(); tagIndex++) {
//            GameDate otherDate = GameDate.fromNode(Node.getNodeForKey(otherTagHis.get(tagIndex), "changed_tag_from"));
//            if (date.compareTo(otherDate) > 0) {
//
//            }
//        }
    }

    @Override
    public void reverse(Node node) {
    }
}

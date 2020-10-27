package com.crschnick.pdx_unlimiter.eu4.format.eu4;

import com.crschnick.pdx_unlimiter.eu4.format.NodeTransformer;
import com.crschnick.pdx_unlimiter.eu4.parser.ArrayNode;
import com.crschnick.pdx_unlimiter.eu4.parser.KeyValueNode;
import com.crschnick.pdx_unlimiter.eu4.parser.Node;
import com.crschnick.pdx_unlimiter.eu4.parser.ValueNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CountryTransformer extends NodeTransformer {

    private void setCustomNationInfo(Node n) {
        Node.addNodeToArray(n, KeyValueNode.create("is_custom",
                new ValueNode(Node.hasKey(n, "custom_nation_points"))));
    }

    private void setTag(KeyValueNode kv) {
        Node.addNodeToArray(kv.getNode(), KeyValueNode.create("tag",
                new ValueNode(kv.getKeyName())));
    }

    private void createNewHistory(Node rootNode) {
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

        formatNewHistory(newHistory);
        removeEmptyHistoryNodes(newHistory);
        createTagHistory(newHistory);
    }

    private void formatNewHistory(ArrayNode newHistory) {
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
    }

    private void removeEmptyHistoryNodes(ArrayNode newHistory) {
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
    }

    private void createTagHistory(ArrayNode newHistory) {
        Map<String,Boolean> resolved = new HashMap<>();
        for (int i = 0; i < newHistory.getNodes().size(); i++) {
            KeyValueNode kv = (KeyValueNode) newHistory.getNodes().get(i);

            Node events = Node.getNodeForKey(kv.getNode(), "events");
            ArrayNode tagHistory = new ArrayNode();
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

            resolved.put(kv.getKeyName(), tagHistory.getNodes().size() == 0);
        }

        while (resolved.containsValue(false)) {
            iterativeTagHistoryStep(resolved, newHistory);
        }
    }

    private void iterativeTagHistoryStep(Map<String,Boolean> resolved, ArrayNode newHistory) {
        for (int i = 0; i < newHistory.getNodes().size(); i++) {
            KeyValueNode kv = (KeyValueNode) newHistory.getNodes().get(i);
            var tagHistory = Node.getNodeArray(Node.getNodeForKey(kv.getNode(), "tag_history"));
            String firstKnownTag = tagHistory.size() > 0 ?
                    Node.getString(Node.getNodeForKey(tagHistory.get(0), "changed_tag_from")) : kv.getKeyName();

            boolean isFirstTagResolved = resolved.get(firstKnownTag);
            if (!isFirstTagResolved) {
                continue;
            }

            Node firstTagHistory = Node.getNodeForKey(Node.getNodeForKey(newHistory, firstKnownTag), "tag_history");
            List<Node> content = Node.getNodeArray(firstTagHistory);

            for (int j = content.size() - 1; j >= 0; j--) {
                tagHistory.add(0, content.get(j));
            }

            resolved.put(kv.getKeyName(), true);
        }
    }

    @Override
    public void transform(Node rootNode) {
        createNewHistory(rootNode);
        ArrayNode ar = (ArrayNode) Node.getNodeForKey(rootNode, "countries");
        for (int i = 0; i < ar.getNodes().size(); i++) {
            KeyValueNode kv = (KeyValueNode) ar.getNodes().get(i);
            setTag(kv);
            setCustomNationInfo(kv.getNode());
        }
    }

    @Override
    public void reverse(Node node) {
    }
}

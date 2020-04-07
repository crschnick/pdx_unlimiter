package com.paradox_challenges.eu4_unlimiter.format;

import com.paradox_challenges.eu4_unlimiter.parser.*;

import java.time.Month;
import java.util.ArrayList;
import java.util.List;

public class DateTransformer extends NodeTransformer {

    public static Node toNode(GameDate date) {
        List<Node> nodes = new ArrayList<>(3);
        nodes.add(KeyValueNode.create("day", new ValueNode<Integer>(date.getDay())));
        nodes.add(KeyValueNode.create("month", new ValueNode<Integer>(date.getMonth().getValue())));
        nodes.add(KeyValueNode.create("year", new ValueNode<Integer>(date.getYear())));
        return new ArrayNode(nodes);
    }

    public static GameDate fromNode(Node node) {
        int day = Integer.parseInt(((ValueNode<String>)Node.getNodeForKey(node, "day")).getValue());
        int month = Integer.parseInt(((ValueNode<String>)Node.getNodeForKey(node, "month")).getValue());
        int year = Integer.parseInt(((ValueNode<String>)Node.getNodeForKey(node, "year")).getValue());
        return new GameDate(day, Month.of(month), year);
    }

    @Override
    public void transform(Node node) {
        KeyValueNode kv = (KeyValueNode) node;
        ValueNode<Object> v = (ValueNode<Object>) kv.getNode();
        GameDate d = null;
        if (v.getValue() instanceof Integer) {
            int i = (int) v.getValue();
            d = GameDate.fromInteger(i);
        }
        if (v.getValue() instanceof String) {
            String s = (String) v.getValue();
            d = GameDate.fromString(s);
        }
        kv.setNode(toNode(d));
    }

    @Override
    public void reverse(Node node) {
        KeyValueNode kv = (KeyValueNode) node;
        kv.setNode(new ValueNode<String>(fromNode(kv.getNode()).toString()));
    }
}

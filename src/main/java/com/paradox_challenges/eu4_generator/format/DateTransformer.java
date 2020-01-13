package com.paradox_challenges.eu4_generator.format;

import com.paradox_challenges.eu4_generator.parser.*;

import java.util.ArrayList;
import java.util.List;

public class DateTransformer extends NodeTransformer {

    public static final DateTransformer INSTANCE = new DateTransformer();

    public Node toNode(GameDate date) {
        List<Node> nodes = new ArrayList<>(3);
        nodes.add(KeyValueNode.create("day", new ValueNode<Integer>(date.getDay())));
        nodes.add(KeyValueNode.create("month", new ValueNode<Integer>(date.getMonth().getValue())));
        nodes.add(KeyValueNode.create("year", new ValueNode<Integer>(date.getYear())));
        return new ArrayNode(nodes);
    }

    @Override
    public Node transformNode(Node node) {
        ValueNode<Object> v = (ValueNode<Object>) node;
        if (v.getValue() instanceof Integer) {
            int i = (int) v.getValue();
            GameDate d = GameDate.fromInteger(i);
            return toNode(d);
        }
        if (v.getValue() instanceof String) {
            String s = (String) v.getValue();
            GameDate d = GameDate.fromString(s);
            return toNode(d);
        }
        return null;
    }
}

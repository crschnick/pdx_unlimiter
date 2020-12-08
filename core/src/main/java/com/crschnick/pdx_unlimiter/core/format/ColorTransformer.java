package com.crschnick.pdx_unlimiter.core.format;

import com.crschnick.pdx_unlimiter.core.parser.ArrayNode;
import com.crschnick.pdx_unlimiter.core.parser.KeyValueNode;
import com.crschnick.pdx_unlimiter.core.parser.Node;
import com.crschnick.pdx_unlimiter.core.parser.ValueNode;

public class ColorTransformer extends NodeTransformer {

    public static final NodeTransformer RECURSIVE_TRANSFORMER = new NodeTransformer() {
        @Override
        public void transform(Node node) {
            new ColorTransformer().transform(node);

            if (node instanceof KeyValueNode) {
                transform(Node.getKeyValueNode(node).getNode());
            } else if (node instanceof ArrayNode) {
                for (Node n : Node.getNodeArray(node)) {
                    transform(n);
                }
            }
        }

        @Override
        public void reverse(Node node) {

        }
    };

    @Override
    public void transform(Node n) {
        if (!(n instanceof ArrayNode)) {
            return;
        }

        ArrayNode a = (ArrayNode) n;
        for (int i = 0; i < a.getNodes().size() - 1; i++) {
            Node typeKvNode = a.getNodes().get(i);
            var typeNode = typeKvNode instanceof KeyValueNode ?
                    ((KeyValueNode) a.getNodes().get(i)).getNode() : a.getNodes().get(i);
            boolean rgb = typeNode instanceof ValueNode && (((ValueNode) typeNode).getValue().equals("rgb") || ((ValueNode) typeNode).getValue().equals("579"));
            boolean hsv = typeNode instanceof ValueNode && ((ValueNode) typeNode).getValue().equals("hsv");
            boolean hsv360 = typeNode instanceof ValueNode && ((ValueNode) typeNode).getValue().equals("hsv360");
            if (!rgb && !hsv && !hsv360) {
                continue;
            }

            var colors = a.getNodes().get(i + 1);
            if (!(colors instanceof ArrayNode) || !(((ArrayNode) colors).getNodes().size() == 3)) {
                continue;
            }

            a.getNodes().remove(typeKvNode);
            a.getNodes().remove(colors);

            ArrayNode replacement = new ArrayNode();
            replacement.getNodes().add(KeyValueNode.create("type", typeNode));
            replacement.getNodes().add(KeyValueNode.create("values", colors));

            String key = typeKvNode instanceof KeyValueNode ?
                    ((KeyValueNode) typeKvNode).getKeyName() : null;
            a.getNodes().add(i, key != null ? KeyValueNode.create(key, replacement) : replacement);
        }

    }

    @Override
    public void reverse(Node node) {

    }
}

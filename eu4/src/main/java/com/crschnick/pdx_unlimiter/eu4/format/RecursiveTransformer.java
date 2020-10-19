package com.crschnick.pdx_unlimiter.eu4.format;

import com.crschnick.pdx_unlimiter.eu4.parser.ArrayNode;
import com.crschnick.pdx_unlimiter.eu4.parser.KeyValueNode;
import com.crschnick.pdx_unlimiter.eu4.parser.Node;

import java.util.function.Predicate;

public class RecursiveTransformer extends NodeTransformer {

    private Predicate<Node> filter;
    private NodeTransformer transformer;

    public RecursiveTransformer(Predicate<Node> filter, NodeTransformer transformer) {
        this.filter = filter;
        this.transformer = transformer;
    }

    @Override
    public void transform(Node node) {
        if (filter.test(node)) {
            transformer.transform(node);
            return;
        }

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
}

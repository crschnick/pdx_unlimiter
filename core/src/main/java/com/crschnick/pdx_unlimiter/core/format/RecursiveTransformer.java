package com.crschnick.pdx_unlimiter.core.format;

import com.crschnick.pdx_unlimiter.core.parser.ArrayNode;
import com.crschnick.pdx_unlimiter.core.parser.KeyValueNode;
import com.crschnick.pdx_unlimiter.core.parser.Node;

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

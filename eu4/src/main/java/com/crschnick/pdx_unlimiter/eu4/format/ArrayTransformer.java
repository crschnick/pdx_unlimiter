package com.crschnick.pdx_unlimiter.eu4.format;

import com.crschnick.pdx_unlimiter.eu4.parser.ArrayNode;
import com.crschnick.pdx_unlimiter.eu4.parser.Node;

public class ArrayTransformer extends NodeTransformer {

    private NodeTransformer transformer;

    public ArrayTransformer(NodeTransformer transformer) {
        this.transformer = transformer;
    }

    @Override
    public void transform(Node node) {
        ArrayNode a = (ArrayNode) node;
        for (Node sub : a.getNodes()) {
            transformer.transform(sub);
        }
    }

    @Override
    public void reverse(Node node) {
        ArrayNode a = (ArrayNode) node;
        for (Node sub : a.getNodes()) {
            transformer.reverse(sub);
        }
    }
}

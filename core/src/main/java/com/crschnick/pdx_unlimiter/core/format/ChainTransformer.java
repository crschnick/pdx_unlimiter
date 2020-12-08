package com.crschnick.pdx_unlimiter.core.format;

import com.crschnick.pdx_unlimiter.core.parser.Node;

import java.util.List;

public class ChainTransformer extends NodeTransformer {

    private List<NodeTransformer> transformers;

    public ChainTransformer(List<NodeTransformer> transformers) {
        this.transformers = transformers;
    }

    @Override
    public void transform(Node node) {
        for (NodeTransformer t : transformers) {
            t.transform(node);
        }
    }

    @Override
    public void reverse(Node node) {
        for (int i = transformers.size(); i >= 0; i--) {
            transformers.get(i).reverse(node);
        }
    }
}

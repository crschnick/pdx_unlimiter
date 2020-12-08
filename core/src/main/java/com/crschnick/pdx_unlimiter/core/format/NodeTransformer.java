package com.crschnick.pdx_unlimiter.core.format;

import com.crschnick.pdx_unlimiter.core.parser.Node;

public abstract class NodeTransformer {

    public abstract void transform(Node node);

    public abstract void reverse(Node node);

}

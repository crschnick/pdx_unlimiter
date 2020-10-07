package com.crschnick.pdx_unlimiter.eu4.format;

import com.crschnick.pdx_unlimiter.eu4.parser.Node;

public abstract class NodeTransformer {

    public abstract void transform(Node node);

    public abstract void reverse(Node node);

}

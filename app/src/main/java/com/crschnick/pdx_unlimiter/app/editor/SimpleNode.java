package com.crschnick.pdx_unlimiter.app.editor;

import com.crschnick.pdx_unlimiter.core.parser.ArrayNode;
import com.crschnick.pdx_unlimiter.core.parser.KeyValueNode;
import com.crschnick.pdx_unlimiter.core.parser.Node;

import java.util.List;
import java.util.Optional;

public class SimpleNode extends EditorNode {

    private int keyIndex;
    private Node backingNode;

    public SimpleNode(EditorNode parent, String keyName, int keyIndex, Node backingNode) {
        super(parent, keyName);
        this.keyIndex = keyIndex;
        this.backingNode = backingNode;
    }

    @Override
    public boolean isReal() {
        return true;
    }

    @Override
    public SimpleNode getRealParent() {
        return getDirectParent().isReal() ? (SimpleNode) getDirectParent() : getDirectParent().getRealParent();
    }

    @Override
    public List<EditorNode> open() {
        return EditorNode.create(this, backingNode.getNodeArray());
    }

    public Node toWritableNode() {
        return backingNode;
    }

    public void update(ArrayNode newNode) {
        getKeyName().ifPresentOrElse(s -> {
            getRealParent().getBackingNode().getNodeArray().set(getKeyIndex(),
                    KeyValueNode.create(s, newNode));
        }, () -> {
            getRealParent().getBackingNode().getNodeArray().set(getKeyIndex(), newNode);
        });
    }

    public int getKeyIndex() {
        return keyIndex;
    }

    public Node getBackingNode() {
        return backingNode;
    }
}

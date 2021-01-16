package com.crschnick.pdx_unlimiter.app.editor;

import com.crschnick.pdx_unlimiter.core.parser.Node;

import java.util.Optional;

public class EditorNode {

    private EditorNode parent;

    private int keyIndex;
    private String keyName;

    private Node node;
    private boolean synthetic;

    public EditorNode(EditorNode parent, int keyIndex, String keyName, Node node, boolean synthetic) {
        this.parent = parent;
        this.keyIndex = keyIndex;
        this.keyName = keyName;
        this.node = node;
        this.synthetic = synthetic;
    }

    public Optional<String> getKeyName() {
        return Optional.ofNullable(keyName);
    }

    public Node getNode() {
        return node;
    }

    public boolean isSynthetic() {
        return synthetic;
    }

    public EditorNode getParent() {
        return parent;
    }

    public int getKeyIndex() {
        return keyIndex;
    }
}

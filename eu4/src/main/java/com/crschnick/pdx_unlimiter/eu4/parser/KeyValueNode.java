package com.crschnick.pdx_unlimiter.eu4.parser;

import com.crschnick.pdx_unlimiter.eu4.format.Namespace;

public class KeyValueNode extends Node {

    public static KeyValueNode create(String keyName, Node node) {
        return new KeyValueNode(keyName, node);
    }

    private String keyName;

    private Node node;

    public KeyValueNode(String keyName, Node node) {
        this.keyName = keyName;
        this.node = node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public String toString(int indentation) {
        return getKeyName() + "=" + getNode().toString(indentation);
    }

    public String getKeyName() {
        return keyName;
    }

    public Node getNode() {
        return node;
    }
}

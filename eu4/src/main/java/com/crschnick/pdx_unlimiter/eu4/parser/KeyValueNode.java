package com.crschnick.pdx_unlimiter.eu4.parser;

public class KeyValueNode extends Node {

    private String keyName;
    private Node node;

    public KeyValueNode(String keyName, Node node) {
        this.keyName = keyName;
        this.node = node;
    }

    public static KeyValueNode create(String keyName, Node node) {
        return new KeyValueNode(keyName, node);
    }

    public String toString() {
        return getKeyName() + "=" + getNode().toString();
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

    public void setNode(Node node) {
        this.node = node;
    }
}

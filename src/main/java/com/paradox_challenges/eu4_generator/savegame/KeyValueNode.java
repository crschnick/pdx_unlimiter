package com.paradox_challenges.eu4_generator.savegame;

import com.paradox_challenges.eu4_generator.format.Namespace;

public class KeyValueNode extends Node {

    public static KeyValueNode create(String keyName, Node node) {
        return new KeyValueNode(keyName, node);
    }

    public static KeyValueNode createWithNamespace(String keyName, Node node, Namespace ns) {
        return new KeyValueNode(ns.getKeyName(keyName), node);
    }

    private String keyName;

    private Node node;

    public KeyValueNode(String keyName, Node node) {
        this.keyName = keyName;
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

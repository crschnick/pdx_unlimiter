package com.crschnick.pdx_unlimiter.core.parser;

public class ValueNode extends Node {

    private String value;

    public ValueNode(String value) {
        this.value = value;
    }

    public String toString() {
        return "ValueNode(" + getValue().toString() + ")";
    }

    public String getValue() {
        return value;
    }
}

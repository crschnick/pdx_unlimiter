package com.crschnick.pdx_unlimiter.core.parser;

public class ValueNode extends Node {

    private boolean string;
    private String value;

    public ValueNode(boolean string, String value) {
        this.string = string;
        this.value = value;
    }

    public String toString() {
        return "ValueNode(" + getValue().toString() + ")";
    }

    public String getValue() {
        return value;
    }

    public boolean isStringValue() {
        return string;
    }
}

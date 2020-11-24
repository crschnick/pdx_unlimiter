package com.crschnick.pdx_unlimiter.eu4.parser;

public class ValueNode extends Node {

    private Object value;

    public ValueNode(Object value) {
        this.value = value;
    }

    public String toString() {
        return "ValueNode(" + getValue().toString() + ")";
    }

    public Object getValue() {
        return value;
    }
}

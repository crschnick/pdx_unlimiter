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

    public Type determineType() {
        if (isStringValue()) {
            return Type.TEXT;
        }

        if (isBoolean()) {
            return Type.BOOLEAN;
        }

        if (isInteger()) {
            return Type.INTEGER;
        }

        if (isDouble()) {
            return Type.FLOATING_POINT;
        }

        return Type.GAME_VALUE;
    }

    public static enum Type {
        TEXT,
        BOOLEAN,
        INTEGER,
        FLOATING_POINT,
        GAME_VALUE
    }
}

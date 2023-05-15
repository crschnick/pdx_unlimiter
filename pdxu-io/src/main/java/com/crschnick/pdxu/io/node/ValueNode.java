package com.crschnick.pdxu.io.node;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

public final class ValueNode extends Node {

    private static final byte DOUBLE_QUOTE_CHAR = 34;
    private static final Pattern LONG = Pattern.compile("[+-]?[0-9]+");
    private static final Pattern DOUBLE = Pattern.compile("[+-]?([0-9]+)[.,]([0-9]+)");

    private NodeContext context;
    private int scalarIndex;

    public ValueNode(String value, boolean quoted) {
        Objects.requireNonNull(value);
        if (!quoted && value.length() == 0) {
            throw new IllegalArgumentException("Unquoted value has to have a length of at least 1");
        }

        this.context = new NodeContext(value, quoted);
        this.scalarIndex = 0;
    }

    public ValueNode(NodeContext context, int scalarIndex) {
        this.context = context;
        this.scalarIndex = scalarIndex;
    }

    public void set(ValueNode newValue) {
        this.context = newValue.context;
        this.scalarIndex = newValue.scalarIndex;
    }

    public boolean isQuoted() {
        if (context.getData().length < 2) {
            return false;
        }

        var b = context.getLiteralsBegin()[scalarIndex];
        return context.getData()[b] == DOUBLE_QUOTE_CHAR &&
                context.getData()[b + context.getLiteralsLength()[scalarIndex] - 1] == DOUBLE_QUOTE_CHAR;
    }

    @Override
    public String toString() {
        return context.evaluate(scalarIndex);
    }

    @Override
    public String toDebugValue() {
        return toString();
    }

    @Override
    public Descriptor describe() {
        ValueType t;
        if (isQuoted()) {
            t = ValueType.TEXT;
        } else {
            var s = evaluateContent();
            if (s.equals("yes") || s.equals("no")) {
                t = ValueType.BOOLEAN;
            } else if (DOUBLE.matcher(s).matches()) {
                t = ValueType.FLOATING_POINT;
            } else if (LONG.matcher(s).matches()) {
                t = ValueType.INTEGER;
            } else {
                t = ValueType.UNQUOTED_STRING;
            }
        }
        return new Descriptor(t, KeyType.NONE);
    }

    @Override
    public void write(NodeWriter writer) throws IOException {
        writer.write(context, scalarIndex);
    }

    @Override
    public boolean getBoolean() {
        return "yes".equals(evaluateContent());
    }

    private String evaluateContent() {
        boolean quoted = isQuoted();
        var s = context.evaluate(scalarIndex);
        if (quoted) {
            return s.substring(1, s.length() - 1);
        } else {
            return s;
        }
    }

    @Override
    public String getString() {
        return evaluateContent();
    }

    @Override
    public int getInteger() {
        return Integer.parseInt(evaluateContent());
    }

    @Override
    public long getLong() {
        return Long.parseLong(evaluateContent());
    }

    @Override
    public double getDouble() {
        // Replaces decimal commas
        return Double.parseDouble(evaluateContent().replaceAll(",", ""));
    }

    @Override
    public boolean isValue() {
        return true;
    }

    @Override
    public boolean isArray() {
        return false;
    }

    @Override
    public boolean isTagged() {
        return false;
    }

    @Override
    public ValueNode getValueNode() {
        return this;
    }

    @Override
    public boolean matches(NodeMatcher matcher) {
        return matcher.matchesScalar(context, scalarIndex);
    }

    @Override
    public Node copy() {
        return new ValueNode(context, scalarIndex);
    }

    public Optional<String> getInlineMathExpression() {
        if (getString().startsWith("@[") && getString().endsWith("]")) {
            return Optional.of(getString().substring(2, getString().length() - 1));
        }

        return Optional.empty();
    }
}

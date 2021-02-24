package com.crschnick.pdx_unlimiter.core.node;

import com.crschnick.pdx_unlimiter.core.parser.NodeWriter;

import java.io.IOException;
import java.util.regex.Pattern;

public final class ValueNode extends Node {

    private int status;
    private final int begin;
    private final int length;
    private Object value;

    public ValueNode(String value, boolean quoted) {
        this.value = value;
        this.status = (quoted ? NodeConstants.QUOTED : 0) + NodeConstants.EVALUATED + NodeConstants.TYPE_STRING;
        begin = -1;
        length = -1;
    }

    public ValueNode(NodeContext context, boolean quoted, int begin, int length) {
        this.value = context;
        this.begin = begin;
        this.length = length;
        this.status = (quoted ? NodeConstants.QUOTED : 0);
    }

    public boolean isQuoted() {
        return (status & NodeConstants.QUOTED) != 0;
    }

    private boolean isEvalulated() {
        return (status & NodeConstants.EVALUATED) != 0;
    }

    private NodeContext getContext() {
        return (NodeContext) value;
    }

    @Override
    public String toString() {
        if (isEvalulated()) {
            return value.toString();
        } else {
            return getContext().evaluate(begin, length);
        }
    }

    @Override
    public Descriptor describe() {
        ValueType t = null;
        if (isEvalulated()) {
            if ((status & NodeConstants.TYPE_BOOLEAN) != 0) {
                t = ValueType.BOOLEAN;
            }
            else if (value instanceof Integer || value instanceof Long) {
                t = ValueType.INTEGER;
            }
            else if (value instanceof Double) {
                t = ValueType.FLOATING_POINT;
            }
            else if (value instanceof String) {
                t = (status & NodeConstants.QUOTED) != 0 ? ValueType.TEXT : ValueType.UNQUOTED_STRING;
            }

            throw new IllegalStateException();
        } else {
            if ((status & NodeConstants.QUOTED) != 0) {
                t = ValueType.TEXT;
            } else {
                var s = evaluateToString();
                if (s.equals("yes") || s.equals("no")) {
                    t = ValueType.BOOLEAN;
                }

                else if (DOUBLE.matcher(s).matches()) {
                    t = ValueType.FLOATING_POINT;
                }

                else if (LONG.matcher(s).matches()) {
                    t = ValueType.INTEGER;
                }

                t = ValueType.UNQUOTED_STRING;
            }
        }
        return new Descriptor(t, KeyType.NONE);
    }

    @Override
    public void write(NodeWriter writer) throws IOException {
        if (isEvalulated()) {
            if (isQuoted()) {
                writer.write("\"");
            }
            writer.write(value.toString());
            if (isQuoted()) {
                writer.write("\"");
            }
        } else {
            writer.write(getContext(), begin, length);
        }
    }

    @Override
    public boolean getBoolean() {
        if (!isEvalulated()) {
            var ctx = getContext();
            byte[] yes = "yes".getBytes(ctx.getCharset());
            boolean b = true;
            for (int i = begin; i < begin + length; i++) {
                if (yes[i - begin] != ctx.getData()[i]) {
                    b = false;
                    break;
                }
            }

            value = b;
            status += NodeConstants.EVALUATED + NodeConstants.TYPE_BOOLEAN;
            return b;
        }

        return (boolean) value;
    }

    private String evaluateToString() {
        boolean quoted = isQuoted();
        return getContext().evaluate(begin + (quoted ? 1 : 0), length - (quoted ? 2 : 0));
    }

    @Override
    public String getString() {
        if (!isEvalulated()) {
            var s = evaluateToString();
            value = s;
            status += NodeConstants.EVALUATED + NodeConstants.TYPE_STRING;
            return s;
        }

        return (String) value;
    }

    @Override
    public int getInteger() {
        if (!isEvalulated()) {
            int n = 0;
            var ctx = getContext();
            for (int i = begin; i < begin + length; i++) {
                n = (10 * n) + (ctx.getData()[i] - '0');
            }
            value = n;
            status += NodeConstants.EVALUATED + NodeConstants.TYPE_INTEGER;
            return n;
        }

        return (int) value;
    }

    @Override
    public long getLong() {
        if (!isEvalulated()) {
            long n = 0;
            var ctx = getContext();
            for (int i = begin; i < begin + length; i++) {
                n = (10 * n) + (ctx.getData()[i] - '0');
            }
            value = n;
            status += NodeConstants.EVALUATED + NodeConstants.TYPE_LONG;
            return n;
        }

        return (long) value;
    }

    @Override
    public double getDouble() {
        if (!isEvalulated()) {
            var s = evaluateToString();
            var d= Double.parseDouble(s);
            value = d;
            status += NodeConstants.EVALUATED + NodeConstants.TYPE_FLOATING_POINT;
            return d;
        }
        return (double) value;
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
    public boolean isColor() {
        return false;
    }

    private static final Pattern LONG = Pattern.compile("-?[0-9]+");
    private static final Pattern DOUBLE = Pattern.compile("-?([0-9]+)\\.([0-9]+)");
}

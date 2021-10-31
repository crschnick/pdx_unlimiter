package com.crschnick.pdxu.io.parser;

import com.crschnick.pdxu.io.node.NodeContext;

public class ParseException extends Exception {

    private static int getUsedOffset(int offset, byte[] data) {
        boolean isEndOfLine = data.length - 2 > offset && data[offset + 1] == '\n';
        if (isEndOfLine) {
            return offset + 2;
        } else {
            return offset;
        }
    }

    private static int getLineNumber(int offset, byte[] data) {
        offset = getUsedOffset(offset, data);

        int line = 1;
        for (int i = 0; i < offset; i++) {
            if (data[i] == '\n') {
                line++;
            }
        }
        return line;
    }

    private static int getDataStart(int offset, byte[] data) {
        offset = getUsedOffset(offset, data);

        int i;
        for (i = offset; i >= Math.max(offset - 30, 0); i--) {
            if (data[i] == '\n') {
                return i + 1;
            }
        }
        return i + 1;
    }

    private static int getDataEnd(int offset, byte[] data) {
        offset = getUsedOffset(offset, data);

        int i;
        for (i = offset; i < Math.min(offset + 30, data.length); i++) {
            if (data[i] == '\n') {
                return i - 1;
            }
        }
        return i - 1;
    }

    public static ParseException create(String s, int offset, byte[] data) {
        var start = getDataStart(offset, data);
        var end = getDataEnd(offset, data);
        var length = end - start + 1;
        var snippet = new String(data, start, length);
        var msg = "Parser failed at line " + getLineNumber(offset, data) + " / offset " + offset + ": " + s + "\n\n" + snippet;
        return new ParseException(msg);
    }

    public static ParseException createFromLiteralIndex(String s, int lIndex, NodeContext ctx) {
        var offset = ctx.getLiteralsBegin()[lIndex];
        return create(s, offset, ctx.getData());
    }

    public ParseException(String message) {
        super(message);
    }

    public ParseException(Throwable t) {
        super("Parser failed because: " + t.getMessage(), t);
    }

    public ParseException(String message, Throwable cause) {
        super(message, cause);
    }
}

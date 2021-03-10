package com.crschnick.pdx_unlimiter.core.node;

import java.nio.charset.Charset;

public final class NodeMatcher {

    private final String matchString;
    private Charset currentCharset;
    private byte[] matchBytes;

    public NodeMatcher(String matchString) {
        this.matchString = matchString;
    }

    public boolean matchesScalar(NodeContext ctx, int index) {
        if (!ctx.getCharset().equals(currentCharset)) {
            matchBytes = matchString.getBytes(ctx.getCharset());
            currentCharset = ctx.getCharset();
        }

        return contains(ctx.getData(), ctx.getLiteralsBegin()[index], ctx.getLiteralsLength()[index], matchBytes);
    }

    private static boolean contains(byte[] array, int start, short length, byte[] toFind) {
        for (int i = start; i < length - toFind.length; ++i) {
            boolean found = true;
            for (int j = 0; j < toFind.length; ++j) {
                if (array[i + j] != toFind[j]) {
                    found = false;
                    break;
                }
            }
            if (found) {
                return true;
            }
        }
        return false;
    }
}

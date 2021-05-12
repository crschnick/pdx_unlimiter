package com.crschnick.pdxu.io.node;

import java.nio.charset.Charset;
import java.util.Arrays;

public abstract class NodeMatcher {

    protected final String matchString;
    protected Charset currentCharset;

    public NodeMatcher(String matchString) {
        this.matchString = matchString;
    }

    public abstract boolean matchesScalar(NodeContext ctx, int index);

    public static final class CaseInsenstiveMatcher extends NodeMatcher {

        private final byte[][] lowerCases;
        private final byte[][] upperCases;
        private int byteSize;

        public CaseInsenstiveMatcher(String matchString) {
            super(matchString);
            lowerCases = new byte[matchString.length()][];
            upperCases = new byte[matchString.length()][];
        }

        private void updateBytes() {
            for (int i = 0; i < matchString.length(); ++i) {
                char c = matchString.charAt(i);
                var b1 = String.valueOf(c).toUpperCase().getBytes(currentCharset);
                upperCases[i] = b1;
                var b2 = String.valueOf(c).toLowerCase().getBytes(currentCharset);
                lowerCases[i] = b2;
            }
            byteSize = matchString.getBytes(currentCharset).length;
        }

        public boolean matchesScalar(NodeContext ctx, int index) {
            if (!ctx.getCharset().equals(currentCharset)) {
                currentCharset = ctx.getCharset();
                updateBytes();
            }

            return contains(ctx.getData(), ctx.getLiteralsBegin()[index], ctx.getLiteralsLength()[index]);
        }

        private boolean contains(byte[] array, int start, short length) {
            for (int i = start; i <= start + length - byteSize; ++i) {
                boolean found = true;

                int byteIndex = i;
                for (int j = 0; j < matchString.length(); ++j) {
                    var lc = lowerCases[j];
                    var uc = upperCases[j];

                    if (!Arrays.equals(array, byteIndex, byteIndex + lc.length, lc, 0, lc.length) &&
                            !Arrays.equals(array, byteIndex, byteIndex + uc.length, uc, 0, uc.length)) {
                        found = false;
                        break;
                    }

                    byteIndex += lc.length;
                }
                if (found) {
                    return true;
                }
            }
            return false;
        }
    }

    public static final class CaseSenstiveMatcher extends NodeMatcher {

        private byte[] matchBytes;

        public CaseSenstiveMatcher(String matchString) {
            super(matchString);
        }

        private boolean contains(byte[] array, int start, short length) {
            for (int i = start; i <= start + length - matchBytes.length; ++i) {
                boolean found = true;
                for (int j = 0; j < matchBytes.length; ++j) {
                    if (array[i + j] != matchBytes[j]) {
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

        public boolean matchesScalar(NodeContext ctx, int index) {
            if (!ctx.getCharset().equals(currentCharset)) {
                matchBytes = matchString.getBytes(ctx.getCharset());
                currentCharset = ctx.getCharset();
            }

            return contains(ctx.getData(), ctx.getLiteralsBegin()[index], ctx.getLiteralsLength()[index]);
        }
    }
}

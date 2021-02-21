package com.crschnick.pdx_unlimiter.core.parser;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class TextFormatTokenizer {

    public static final byte STRING_UNQUOTED = 1;
    public static final byte STRING_QUOTED = 2;
    public static final byte OPEN_GROUP = 3;
    public static final byte CLOSE_GROUP = 4;
    public static final byte EQUALS = 5;

    private Charset charset;
    private byte[] bytes;
    private boolean isInQuotes;
    private boolean isInComment;
    private int prev;
    private int i;
    private byte[] tokenTypes;
    private List<String> stringLiterals;
    private int tokenCounter;
    private Stack<Integer> arraySizeStack;
    private int[] arraySizes;
    private int arraySizesCounter;

    public TextFormatTokenizer(Charset charset, byte[] bytes) {
        this.charset = charset;
        this.bytes = bytes;
        this.prev = 0;
        this.tokenCounter = 0;
        this.tokenTypes = new byte[bytes.length / 2];
        this.stringLiterals = new ArrayList<>(bytes.length / 10);
        this.arraySizeStack = new Stack<>();
        this.arraySizes = new int[bytes.length / 10];
        this.arraySizesCounter = 0;
    }

    public void tokenize() {
        tokenTypes[0] = OPEN_GROUP;
        arraySizes[0] = 0;
        arraySizeStack.add(0);
        arraySizesCounter++;
        tokenCounter = 1;
        for (i = 0; i <= bytes.length; i++) {
            tokenizeIteration();
        }
        tokenTypes[tokenCounter] = CLOSE_GROUP;
    }

    private void tokenizeIteration() {
        // Add extra new line at the end to simulate end of token
        char c = i == bytes.length ? '\n' : (char) bytes[i];
        byte t = 0;
        if (isInQuotes && c != '"') {
            return;
        } else if (c == '"') {
            isInQuotes = !isInQuotes;
        } else if (c == '{') {
            t = OPEN_GROUP;
        } else if (c == '}') {
            t = CLOSE_GROUP;
        } else if (c == '=') {
            t = EQUALS;
        }

        if (isInComment) {
            if (c == '\n') {
                isInComment = false;
            }
            prev = i + 1;
            return;
        }

        boolean isWhitespace = !isInQuotes && (c == '\n' || c == '\r' || c == ' ' || c == '\t');
        boolean isComment = c == '#';
        boolean marksEndOfPreviousToken =
                (t != 0 && prev < i)                // New token finishes old token
                        || (isWhitespace && prev < i)          // Whitespace finishes old token
                        || (isComment && prev < i);            // New comment finishes old token
        if (marksEndOfPreviousToken) {
            int offset;
            int length;
            boolean quoted;
            if (bytes[prev] == '"' && bytes[i - 1] == '"') {
                quoted = true;
                offset = prev + 1;
                length = (i - 2) - (prev + 1) + 1;
                tokenTypes[tokenCounter++] = STRING_QUOTED;
            } else {
                quoted = false;
                length = (i - 1) - (prev) + 1;
                offset = prev;
                tokenTypes[tokenCounter++] = STRING_UNQUOTED;
            }
            var s = new String(bytes, offset, length, charset);

            // Increase array size whenever a scalar is found
            arraySizes[arraySizeStack.peek()]++;

            // Intern any short strings like country tags
            // Also intern any unquoted value like key names and game specific values
            if (!quoted || s.length() < 4) {
                s = s.intern();
            }

            stringLiterals.add(s);
        }

        if (isWhitespace) {
            prev = i + 1;
        } else if (t != 0) {
            if (t == CLOSE_GROUP) {
                arraySizeStack.pop();
            } else if (t == EQUALS) {
                arraySizes[arraySizeStack.peek()]--;
            } else if (t == OPEN_GROUP) {
                arraySizes[arraySizeStack.peek()]++;
                arraySizeStack.add(arraySizesCounter++);
            }

            tokenTypes[tokenCounter++] = t;
            prev = i + 1;
        } else if (isComment) {
            isInComment = true;
        }
    }

    public byte[] getTokenTypes() {
        return tokenTypes;
    }

    public List<String> getStringLiterals() {
        return stringLiterals;
    }

    public int[] getArraySizes() {
        return arraySizes;
    }
}

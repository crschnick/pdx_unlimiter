package com.crschnick.pdx_unlimiter.core.parser;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TextFormatParser extends FormatParser {

    private Charset charset;

    private TextFormatParser(Charset charset) {
        this.charset = charset;
    }

    public static TextFormatParser textFileParser() {
        return new TextFormatParser(StandardCharsets.UTF_8);
    }

    public static TextFormatParser ck3SavegameParser() {
        return new TextFormatParser(StandardCharsets.UTF_8);
    }

    public static TextFormatParser eu4SavegameParser() {
        return new TextFormatParser(StandardCharsets.ISO_8859_1);
    }

    public static TextFormatParser stellarisSavegameParser() {
        return new TextFormatParser(StandardCharsets.UTF_8);
    }

    public static TextFormatParser hoi4SavegameParser() {
        return new TextFormatParser(StandardCharsets.UTF_8);
    }

    private List<Token> tokenizeInternal(byte[] bytes) {
        // Approx amount of needed tokens
        List<Token> tokens = new ArrayList<>(bytes.length / 5);
        int prev = 0;
        boolean isInQuotes = false;
        boolean isInComment = false;

        for (int i = 0; i <= bytes.length; i++) {
            // Add extra new line at the end to simulate end of token
            char c = i == bytes.length ? '\n' : (char) bytes[i];
            Token t = null;
            if (isInQuotes && c != '"') {
                continue;
            } else if (c == '"') {
                isInQuotes = !isInQuotes;
            } else if (c == '{') {
                t = new OpenGroupToken();
            } else if (c == '}') {
                t = new CloseGroupToken();
            } else if (c == '=') {
                t = new EqualsToken();
            }

            if (isInComment) {
                if (c == '\n') {
                    isInComment = false;
                }
                prev = i + 1;
                continue;
            }

            boolean isWhitespace = !isInQuotes && (c == '\n' || c == '\r' || c == ' ' || c == '\t');
            boolean isComment = c == '#';
            boolean eof = i == bytes.length - 1;
            boolean marksEndOfPreviousToken =
                    (t != null && prev < i)             // New token finishes old token
                            || (isWhitespace && prev < i)          // Whitespace finishes old token
                            || (isComment && prev < i);            // New comment finishes old token
            if (marksEndOfPreviousToken) {
                var sub = Arrays.copyOfRange(bytes, prev, i);
                if (sub[0] == '"' && sub[sub.length - 1] == '"') {
                    tokens.add(new ValueToken(true, new String(Arrays.copyOfRange(sub, 1, sub.length - 1), charset)));
                } else {
                    tokens.add(new ValueToken(false, new String(sub, StandardCharsets.UTF_8)));
                }
            }

            if (isWhitespace) {
                prev = i + 1;
            } else if (t != null) {
                tokens.add(t);
                prev = i + 1;
            } else if (isComment) {
                isInComment = true;
            }
        }
        return tokens;
    }

    @Override
    public List<FormatParser.Token> tokenize(byte[] data) throws IOException {
        return tokenizeInternal(data);
    }
}

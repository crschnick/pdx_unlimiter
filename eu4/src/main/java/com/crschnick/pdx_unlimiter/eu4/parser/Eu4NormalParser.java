package com.crschnick.pdx_unlimiter.eu4.parser;

import com.crschnick.pdx_unlimiter.eu4.format.Namespace;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Eu4NormalParser extends GamedataParser {

    public static final byte[] MAGIC = new byte[]{0x45, 0x55, 0x34, 0x74, 0x78, 0x74};

    public Eu4NormalParser() {
        super(MAGIC, Namespace.EMPTY);
    }

    private List<Token> tokenize(String s) {
        List<Token> tokens = new ArrayList<>();
        int prev = 0;
        boolean isInQuotes = false;
        for (int i = 0; i < s.length(); i++) {
            Token t = null;
            if (s.charAt(i) == '{') {
                t = new OpenGroupToken();
            } else if (s.charAt(i) == '}') {
                t = new CloseGroupToken();
            } else if (s.charAt(i) == '=') {
                t = new EqualsToken();
            } else if (s.charAt(i) == '"') {
                isInQuotes = !isInQuotes;
            }

            boolean isWhitespace = !isInQuotes && (s.charAt(i) == '\n' || s.charAt(i) == '\r' || s.charAt(i) == ' ' || s.charAt(i) == '\t');
            boolean marksEndOfPreviousToken =
                    (s.charAt(i) == '\0' && prev < i) // EOF
                            || (t != null && prev < i)        // New token finishes old token
                            || (isWhitespace && prev < i);    // Whitespace finishes old token
            if (marksEndOfPreviousToken) {
                String sub = s.substring(prev, i);
                if (sub.equals("yes")) {
                    tokens.add(new ValueToken(true));
                } else if (sub.equals("no")) {
                    tokens.add(new ValueToken(false));
                } else if (Pattern.matches("-?[0-9]+", sub)) {
                    tokens.add(new ValueToken(Long.parseLong(sub)));
                } else if (Pattern.matches("([0-9]*)\\.([0-9]*)", sub)) {
                    tokens.add(new ValueToken(Double.valueOf(sub)));
                } else if (sub.startsWith("\"") && sub.endsWith("\"")) {
                    if (sub.length() == 1) {
                        int a = 0;
                    }
                    tokens.add(new ValueToken(sub.substring(1, sub.length() - 1)));
                } else {
                    tokens.add(new ValueToken(sub));
                }
            }

            if (isWhitespace) {
                prev = i + 1;
                continue;
            } else if (t != null) {
                tokens.add(t);
                prev = i + 1;
            }
        }
        return tokens;
    }

    @Override
    public List<GamedataParser.Token> tokenize(InputStream stream) throws IOException {
        String s = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        s += "\0";
        return tokenize(s);
    }
}

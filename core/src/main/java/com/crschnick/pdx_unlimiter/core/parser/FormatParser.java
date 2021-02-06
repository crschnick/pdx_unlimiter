package com.crschnick.pdx_unlimiter.core.parser;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public abstract class FormatParser {

    public static boolean validateHeader(byte[] header, InputStream stream) throws IOException {
        byte[] first = new byte[header.length];
        stream.readNBytes(first, 0, header.length);
        return Arrays.equals(first, header);
    }

    public static boolean validateHeader(byte[] header, byte[] content) {
        byte[] first = new byte[header.length];
        System.arraycopy(content, 0, first, 0, header.length);
        return Arrays.equals(first, header);
    }

    public abstract List<Token> tokenize(byte[] data) throws IOException;

    public final ArrayNode parse(Path in) throws IOException {
        return parse(Files.readAllBytes(in));
    }

    public final ArrayNode parse(byte[] input) throws IOException {
        List<Token> tokens = tokenize(input);

        tokens.add(0, new OpenGroupToken());
        tokens.add(new CloseGroupToken());
        return hierachiseTokens(tokens);
    }

    private ArrayNode hierachiseTokens(List<Token> tokens) {
        Map.Entry<Node, Integer> node = createNode(tokens, 0);
        return (ArrayNode) node.getKey();
    }

    private Map.Entry<Node, Integer> createNode(List<Token> tokens, int index) {
        if (tokens.get(index).getType() == TokenType.VALUE) {
            var vt = ((ValueToken) tokens.get(index));
            String obj = vt.value;

            boolean isColor = !vt.quoted && ColorNode.isColorName(vt.value);
            if (isColor) {
                return new AbstractMap.SimpleEntry<>(new ColorNode(vt.value, List.of(
                        new ValueNode(false, ((ValueToken) tokens.get(index + 2)).value),
                        new ValueNode(false, ((ValueToken) tokens.get(index + 3)).value),
                        new ValueNode(false, ((ValueToken) tokens.get(index + 4)).value))), index + 6);
            } else {
                return new AbstractMap.SimpleEntry<>(new ValueNode(vt.quoted, obj), index + 1);
            }
        }

        if (tokens.get(index).getType() == TokenType.EQUALS) {
            throw new IllegalStateException("Encountered unexpected =");
        }

        List<Node> childs = new ArrayList<>();
        int currentIndex = index + 1;
        while (true) {
            if (currentIndex == tokens.size()) {
                throw new IllegalStateException("Reached EOF but found no closing group token");
            }

            if (tokens.get(currentIndex).getType() == TokenType.CLOSE_GROUP) {
                return new AbstractMap.SimpleEntry<>(new ArrayNode(childs), currentIndex + 1);
            }

            if (tokens.get(currentIndex).getType() == TokenType.VALUE) {
                var vt = ((ValueToken) tokens.get(currentIndex));

                //Special case for missing "="
                boolean isKeyValueWithoutEquals =
                        !ColorNode.isColorName(vt.value)
                        && !vt.quoted
                        && tokens.get(currentIndex + 1).getType() == TokenType.OPEN_GROUP
                        && vt.value.matches("\\w+");
                if (isKeyValueWithoutEquals) {
                    tokens.add(currentIndex + 1, EqualsToken.INSTANCE);
                }
            }

            boolean isKeyValue = tokens.get(currentIndex + 1).getType() == TokenType.EQUALS;
            if (isKeyValue) {
                String realKey = null;
                Object value = ((ValueToken) tokens.get(currentIndex)).value;
                realKey = value.toString();

                Map.Entry<Node, Integer> result = createNode(tokens, currentIndex + 2);
                childs.add(KeyValueNode.create(realKey, result.getKey()));
                currentIndex = result.getValue();
            } else {
                Map.Entry<Node, Integer> result = createNode(tokens, currentIndex);
                currentIndex = result.getValue();
                childs.add(result.getKey());
            }
        }
    }

    enum TokenType {
        VALUE,
        OPEN_GROUP,
        CLOSE_GROUP,
        EQUALS
    }

    public  static abstract class Token {
        abstract TokenType getType();
    }

    public static class ValueToken extends Token {

        boolean quoted;
        String value;

        public ValueToken(boolean quoted, String value) {
            this.quoted = quoted;
            this.value = value;
        }

        @Override
        TokenType getType() {
            return TokenType.VALUE;
        }
    }

    public static class EqualsToken extends Token {

        public static EqualsToken INSTANCE = new EqualsToken();

        @Override
        TokenType getType() {
            return TokenType.EQUALS;
        }
    }

    public static class OpenGroupToken extends Token {

        public static OpenGroupToken INSTANCE = new OpenGroupToken();

        @Override
        TokenType getType() {
            return TokenType.OPEN_GROUP;
        }
    }

    public static class CloseGroupToken extends Token {

        public static CloseGroupToken INSTANCE = new CloseGroupToken();

        @Override
        TokenType getType() {
            return TokenType.CLOSE_GROUP;
        }
    }
}

package com.paradox_challenges.eu4_unlimiter.parser;

import com.paradox_challenges.eu4_unlimiter.format.Namespace;

import java.io.*;
import java.util.*;

public abstract class GamedataParser {

    enum TokenType {
        VALUE,
        OPEN_GROUP,
        CLOSE_GROUP,
        EQUALS
    }

    public abstract class Token {
        abstract TokenType getType();
    }

    public class ValueToken<T> extends Token {

        T value;

        public ValueToken(T v) {
            value = v;
        }

        @Override
        TokenType getType() {
            return TokenType.VALUE;
        }
    }

    public class EqualsToken extends Token {

        @Override
        TokenType getType() {
            return TokenType.EQUALS;
        }
    }

    public class OpenGroupToken extends Token {

        @Override
        TokenType getType() {
            return TokenType.OPEN_GROUP;
        }
    }

    public class CloseGroupToken extends Token {

        @Override
        TokenType getType() {
            return TokenType.CLOSE_GROUP;
        }
    }

    private byte[] header;

    private Namespace namespace;

    public GamedataParser(byte[] header, Namespace namespace) {
        this.header = header;
        this.namespace = namespace;
    }

    public abstract List<Token> tokenize(InputStream stream) throws IOException;

    public final Optional<Node> parse(InputStream stream) throws IOException {
        byte[] first = new byte[header.length];
        stream.readNBytes(first, 0, header.length);
        if (!Arrays.equals(first, header)) {
            stream.close();
            return Optional.empty();
        }

        List<Token> tokens = tokenize(stream);

        stream.close();
        tokens.add(0, new OpenGroupToken());
        tokens.add(new CloseGroupToken());
        Node result = hierachiseTokens(tokens);
        return Optional.of(result);
    }

    private Node hierachiseTokens(List<Token> tokens) {
        Map.Entry<Node,Integer> node = createNode(tokens, 0);
        return node.getKey();
    }

    private Map.Entry<Node,Integer> createNode(List<Token> tokens, int index) {
        if (tokens.get(index).getType() == TokenType.VALUE) {
            return new AbstractMap.SimpleEntry<>(new ValueNode<Object>(((ValueToken<Object>) tokens.get(index)).value), index + 1);
        }

        List<Node> childs = new LinkedList<>();
        int currentIndex = index + 1;
        while (true) {
            if (tokens.get(currentIndex).getType() == TokenType.CLOSE_GROUP) {
                return new AbstractMap.SimpleEntry<>(new ArrayNode(childs), currentIndex + 1);
            }

            //Special case for missing "="
            boolean isKeyValueWithoutEquals = tokens.get(currentIndex).getType() == TokenType.VALUE && tokens.get(currentIndex + 1).getType() == TokenType.OPEN_GROUP;
            if (isKeyValueWithoutEquals) {
                tokens.add(currentIndex + 1, new EqualsToken());
            }

            boolean isKeyValue = tokens.get(currentIndex + 1).getType() == TokenType.EQUALS;
            if (isKeyValue) {
                String key = ((ValueToken<Object>) tokens.get(currentIndex)).value.toString();
                Map.Entry<Node,Integer> result = createNode(tokens, currentIndex + 2);
                childs.add(KeyValueNode.createWithNamespace(key, result.getKey(), namespace));
                currentIndex = result.getValue();
            } else {
                Map.Entry<Node,Integer> result = createNode(tokens, currentIndex);
                childs.add(result.getKey());
                currentIndex = result.getValue();
            }
        }
    }
}

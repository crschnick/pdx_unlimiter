package com.crschnick.pdx_unlimiter.eu4.parser;

import com.crschnick.pdx_unlimiter.eu4.format.Namespace;
import com.crschnick.pdx_unlimiter.eu4.io.SavegameWriter;
import com.crschnick.pdx_unlimiter.eu4.savegame.SavegameParseException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public abstract class FormatParser {

    public static final int TOKEN_SLEEP_INTERVAL = 10000;
    public static final int SLEEP_TIME = 2;

    private byte[] header;
    private Namespace namespace;
    private int lastSleep = 0;

    public FormatParser(byte[] header, Namespace namespace) {
        this.header = header;
        this.namespace = namespace;
    }

    public boolean validateHeader(InputStream stream) throws IOException {
        byte[] first = new byte[header.length];
        stream.readNBytes(first, 0, header.length);
        if (!Arrays.equals(first, header)) {
            stream.close();
            return false;
        }

        return true;
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
        lastSleep = 0;
        Map.Entry<Node, Integer> node = createNode(tokens, 0);
        return node.getKey();
    }

    private Map.Entry<Node, Integer> createNode(List<Token> tokens, int index) {
        if (tokens.size() - lastSleep > TOKEN_SLEEP_INTERVAL) {
            try {
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            lastSleep = tokens.size();
        }

        if (tokens.get(index).getType() == TokenType.VALUE) {
            Object obj = ((ValueToken) tokens.get(index)).value;
            return new AbstractMap.SimpleEntry<>(new ValueNode(obj), index + 1);
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

            //Special case for missing "="
            boolean isKeyValueWithoutEquals = tokens.get(currentIndex).getType() == TokenType.VALUE
                    && ((ValueToken) tokens.get(currentIndex)).value instanceof String
                    && tokens.get(currentIndex + 1).getType() == TokenType.OPEN_GROUP;
            if (isKeyValueWithoutEquals) {
                tokens.add(currentIndex + 1, new EqualsToken());
            }

            boolean isKeyValue = tokens.get(currentIndex + 1).getType() == TokenType.EQUALS;
            if (isKeyValue) {
                String realKey = null;
                if (!(((ValueToken) tokens.get(currentIndex)).value instanceof String)) {
                    realKey = ((ValueToken) tokens.get(currentIndex)).value.toString();
                } else {
                    realKey = namespace.getKeyName(((ValueToken) tokens.get(currentIndex)).value.toString());
                }

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

    public abstract class Token {
        abstract TokenType getType();
    }

    public class ValueToken extends Token {

        Object value;

        public ValueToken(Object v) {
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
}

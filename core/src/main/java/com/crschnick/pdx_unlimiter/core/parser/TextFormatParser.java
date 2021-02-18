package com.crschnick.pdx_unlimiter.core.parser;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class TextFormatParser extends FormatParser {

    private Charset charset;
    private int index;
    private int slIndex;
    private TextFormatTokenizer tokenizer;

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

    public final ArrayNode parse(byte[] input) {
        this.tokenizer = new TextFormatTokenizer(charset, input);
        this.tokenizer.tokenize();

        Map.Entry<Node, Integer> node = parseNode();
        return (ArrayNode) node.getKey();
    }

    private Map.Entry<Node, Integer> parseNode() {
        var tt = tokenizer.getTokenTypes();
        var sl = tokenizer.getStringLiterals();
        if (tt[index] == TextFormatTokenizer.STRING_UNQUOTED) {
            String val = sl.get(slIndex++);
            boolean isColor = ColorNode.isColorName(val);
            if (isColor) {
                return new AbstractMap.SimpleEntry<>(new ColorNode(val, List.of(
                        new ValueNode(false, sl.get(slIndex++)),
                        new ValueNode(false, sl.get(slIndex++)),
                        new ValueNode(false, sl.get(slIndex++)))), index + 6);
            } else {
                return new AbstractMap.SimpleEntry<>(new ValueNode(false, val), index + 1);
            }
        } else if (tt[index] == TextFormatTokenizer.STRING_QUOTED) {
            String val = sl.get(slIndex++);
            return new AbstractMap.SimpleEntry<>(new ValueNode(true, val), index + 1);
        }

//        if (tt[index] == TextFormatTokenizer.EQUALS) {
//            throw new IllegalStateException("Encountered unexpected =");
//        }
//
//        if (tt[index] == TextFormatTokenizer.CLOSE_GROUP) {
//            throw new IllegalStateException("Encountered unexpected }");
//        }

        return parseGroup();
    }

    private Map.Entry<Node, Integer> parseGroup() {
        var tt = tokenizer.getTokenTypes();
        List<Node> children = new ArrayList<>();

//        if (tt[index] != TextFormatTokenizer.OPEN_GROUP) {
//            throw new IllegalStateException("Expected {");
//        }

        int currentIndex = index + 1;
        while (true) {
            if (currentIndex == tt.length) {
                throw new IllegalStateException("Reached EOF but found no closing group token");
            }

            if (tt[currentIndex] == TextFormatTokenizer.CLOSE_GROUP) {
                return new AbstractMap.SimpleEntry<>(new ArrayNode(children), currentIndex + 1);
            }

            boolean isKeyValue = tt[currentIndex + 1] == TextFormatTokenizer.EQUALS;
            if (isKeyValue) {
                index = currentIndex + 2;
                String realKey = tokenizer.getStringLiterals().get(slIndex++);
                Map.Entry<Node, Integer> result = parseNode();
                children.add(KeyValueNode.create(realKey, result.getKey()));
                currentIndex = result.getValue();
                continue;
            }

            boolean isKeyValueWithoutEquals = tt[currentIndex] == TextFormatTokenizer.STRING_UNQUOTED &&
                    tt[currentIndex + 1] == TextFormatTokenizer.OPEN_GROUP;
            if (isKeyValueWithoutEquals) {
                index = currentIndex + 1;
                String realKey = tokenizer.getStringLiterals().get(slIndex++);
                Map.Entry<Node, Integer> result = parseNode();
                children.add(KeyValueNode.create(realKey, result.getKey()));
                currentIndex = result.getValue();
                continue;
            }

            index = currentIndex;
            Map.Entry<Node, Integer> result = parseNode();
            currentIndex = result.getValue();
            children.add(result.getKey());
        }
    }
}

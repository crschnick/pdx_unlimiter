package com.crschnick.pdx_unlimiter.core.parser;

import com.crschnick.pdx_unlimiter.core.node.*;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class TextFormatParser extends FormatParser {

    private final Charset charset;
    private int index;
    private int slIndex;
    private int arrayIndex;
    private TextFormatTokenizer tokenizer;
    private NodeContext context;

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

    public final Node parse(byte[] input) {
        this.context = new NodeContext(input, charset);
        this.tokenizer = new TextFormatTokenizer(input);
        this.tokenizer.tokenize();
        var r = parseNode();
        return r;
    }

    private Node parseNode() {
        var tt = tokenizer.getTokenTypes();
        if (tt[index] == TextFormatTokenizer.STRING_UNQUOTED) {
            var begin = tokenizer.getScalarsStart()[slIndex];
            var length = tokenizer.getScalarsLength()[slIndex];

            index++;
            slIndex++;

            boolean isColor = ColorNode.isColorName(context, begin, length);
            if (isColor) {
                var cn = new ColorNode(context.evaluate(begin, length), List.of(
                        new ValueNode(
                                context,
                                false,
                                tokenizer.getScalarsStart()[slIndex],
                                tokenizer.getScalarsLength()[slIndex]),
                        new ValueNode(
                                context,
                                false,
                                tokenizer.getScalarsStart()[slIndex + 1],
                                tokenizer.getScalarsLength()[slIndex + 1]),
                        new ValueNode(
                                context,
                                false,
                                tokenizer.getScalarsStart()[slIndex + 2],
                                tokenizer.getScalarsLength()[slIndex + 2])));

                // A color is also an array, so we have to move the array index!
                arrayIndex++;

                slIndex+=3;

                return cn;
            } else {
                var node = new ValueNode(
                        context,
                        false,
                        begin,
                        length);
                return node;
            }
        } else if (tt[index] == TextFormatTokenizer.STRING_QUOTED) {
            var node = new ValueNode(context, true,
                    tokenizer.getScalarsStart()[slIndex],
                    tokenizer.getScalarsLength()[slIndex]);
            slIndex++;
            index++;
            return node;
        }

        assert tt[index] != TextFormatTokenizer.EQUALS: "Encountered unexpected =";
        assert tt[index] != TextFormatTokenizer.CLOSE_GROUP: "Encountered unexpected }";

        return parseArray();
    }

    private Node parseArray() {
        var tt = tokenizer.getTokenTypes();

        assert tt[index] == TextFormatTokenizer.OPEN_GROUP: "Expected {";
        index++;

        var size = tokenizer.getArraySizes()[arrayIndex++];
        var builder = new ArrayNode.Builder(context, size);
        while (true) {
            assert index < tt.length: "Reached EOF but found no closing group token";

            if (tt[index] == TextFormatTokenizer.CLOSE_GROUP) {
                assert size >= builder.getUsedSize():
                        "Invalid array size. Expected: <= " + size + ", got: " + builder.getUsedSize();
                index++;
                return builder.build();
            }

            boolean isKeyValue = tt[index + 1] == TextFormatTokenizer.EQUALS;
            if (isKeyValue) {
                assert tt[index] == TextFormatTokenizer.STRING_UNQUOTED: "Expected unquoted key";

                int start = tokenizer.getScalarsStart()[slIndex];
                int length = tokenizer.getScalarsLength()[slIndex];
                slIndex++;
                index += 2;

                Node result = parseNode();

                //System.out.println("key: " + context.evaluate(start, length));
                //System.out.println("val: " + result.toString());

                builder.put(start, length, result);
                continue;
            }

            boolean isKeyValueWithoutEquals = tt[index] == TextFormatTokenizer.STRING_UNQUOTED &&
                    tt[index + 1] == TextFormatTokenizer.OPEN_GROUP;
            if (isKeyValueWithoutEquals) {
                index++;
                Node result = parseNode();
                builder.put(tokenizer.getScalarsStart()[slIndex], tokenizer.getScalarsLength()[slIndex], result);
                slIndex++;
                continue;
            }

            // Parse unnamed array element
            Node result = parseNode();
            builder.put(result);
        }
    }

    public Charset getCharset() {
        return charset;
    }
}

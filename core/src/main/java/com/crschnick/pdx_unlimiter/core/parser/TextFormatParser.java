package com.crschnick.pdx_unlimiter.core.parser;

import com.crschnick.pdx_unlimiter.core.node.*;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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

    private void reset() {
        this.index = 0;
        this.slIndex = 0;
        this.arrayIndex = 0;
        this.tokenizer = null;
        this.context = null;
    }

    public final Node parse(byte[] input) {
        this.tokenizer = new TextFormatTokenizer(input);

        var now = Instant.now();
        this.tokenizer.tokenize();
        // System.out.println("Tokenizer took " + ChronoUnit.MILLIS.between(now, Instant.now()) + "ms");

        this.context = new NodeContext(input, charset,
                tokenizer.getScalarsStart(),
                tokenizer.getScalarsLength(),
                tokenizer.getScalarCount());

        now = Instant.now();
        var r = parseArray();
        // System.out.println("Node creator took " + ChronoUnit.MILLIS.between(now, Instant.now()) + "ms");

        reset();

        return r;
    }

    private Node parseNodeIfNotSimpleValue() {
        var tt = tokenizer.getTokenTypes();
        if (tt[index] == TextFormatTokenizer.STRING_UNQUOTED) {
            boolean isColor = ColorNode.isColorName(context, slIndex);

            if (isColor) {
                var type = context.evaluate(slIndex);
                index++;
                slIndex++;
                var cn = new ColorNode(type, List.of(
                        new ValueNode(
                                context,
                                slIndex),
                        new ValueNode(
                                context,
                                slIndex + 1),
                        new ValueNode(
                                context,
                                slIndex + 2)));

                // A color is also an array, so we have to move the array index!
                arrayIndex++;

                slIndex+=3;
                index+=5;

                return cn;
            }
        } else {
            assert tt[index] != TextFormatTokenizer.EQUALS: "Encountered unexpected =";
            assert tt[index] != TextFormatTokenizer.CLOSE_GROUP: "Encountered unexpected }";

            if (tt[index] == TextFormatTokenizer.OPEN_GROUP) {
                return parseArray();
            }
        }

        return null;
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
                assert tt[index] == TextFormatTokenizer.STRING_UNQUOTED ||
                        tt[index] == TextFormatTokenizer.STRING_QUOTED: "Expected key";

                int keyIndex = slIndex;
                slIndex++;
                index += 2;

                Node result = parseNodeIfNotSimpleValue();
                if (result == null) {
                    builder.putKeyAndScalarValue(keyIndex, slIndex);
                    index++;
                    slIndex++;
                } else {
                    builder.putKeyAndNodeValue(keyIndex, result);
                }

                //System.out.println("key: " + context.evaluate(start, length));
                //System.out.println("val: " + result.toString());

                continue;
            }

            boolean isKeyValueWithoutEquals = tt[index] == TextFormatTokenizer.STRING_UNQUOTED &&
                    tt[index + 1] == TextFormatTokenizer.OPEN_GROUP;
            if (isKeyValueWithoutEquals) {
                int keyIndex = slIndex;
                slIndex++;
                index++;
                Node result = parseNodeIfNotSimpleValue();
                assert result != null: "KeyValue without equal sign must be an array node";
                builder.putKeyAndNodeValue(keyIndex, result);

                continue;
            }

            // Parse unnamed array element
            Node result = parseNodeIfNotSimpleValue();
            if (result == null) {
                builder.putScalarValue(slIndex);
                index++;
                slIndex++;
            } else {
                builder.putNodeValue(result);
            }
        }
    }

    public Charset getCharset() {
        return charset;
    }
}

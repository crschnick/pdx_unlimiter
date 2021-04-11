package com.crschnick.pdx_unlimiter.core.parser;

import com.crschnick.pdx_unlimiter.core.info.GameColor;
import com.crschnick.pdx_unlimiter.core.node.*;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class TextFormatParser extends FormatParser {

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

    public final ArrayNode parse(byte[] input) throws ParseException {
        try {
            this.tokenizer = new TextFormatTokenizer(input);

            var now = Instant.now();
            this.tokenizer.tokenize();
            // System.out.println("Tokenizer took " + ChronoUnit.MILLIS.between(now, Instant.now()) + "ms");

            this.context = new NodeContext(input, charset,
                    tokenizer.getScalarsStart(),
                    tokenizer.getScalarsLength(),
                    tokenizer.getScalarCount());

            now = Instant.now();
            ArrayNode r = parseArray();
            // System.out.println("Node creator took " + ChronoUnit.MILLIS.between(now, Instant.now()) + "ms");

            return r;
        } catch (Throwable t) {
            // Catch also errors!
            throw new ParseException(t);
        } finally {
            // Always reset!
            reset();
        }
    }

    private Node parseNodeIfNotSimpleValue() {
        var tt = tokenizer.getTokenTypes();
        if (tt[index] == TextFormatTokenizer.STRING_UNQUOTED) {
            var colorType = tt[index + 1] == TextFormatTokenizer.OPEN_GROUP ?
                    GameColor.getColorType(context, slIndex) : null;

            if (colorType != null) {
                assert tt[index + 1] == TextFormatTokenizer.OPEN_GROUP : "Expected {";
                assert tt[index + 1 + colorType.getComponents() + 1] == TextFormatTokenizer.CLOSE_GROUP : "Expected }";

                // Move other color id
                index++;
                slIndex++;

                // Move over opening {
                index++;

                List<ValueNode> components = new ArrayList<>(colorType.getComponents());
                for (int i = 0; i < colorType.getComponents(); i++) {
                    components.add(new ValueNode(context, slIndex));
                    slIndex++;
                    index++;
                }

                // Move over closing }
                index++;

                // A color is also an array, so we have to move the array index!
                arrayIndex++;

                return new ColorNode(colorType, components);
            }
        } else {
            assert tt[index] != TextFormatTokenizer.EQUALS : "Encountered unexpected =";
            assert tt[index] != TextFormatTokenizer.CLOSE_GROUP : "Encountered unexpected }";

            if (tt[index] == TextFormatTokenizer.OPEN_GROUP) {
                return parseArray();
            }
        }

        return null;
    }

    private ArrayNode parseArray() {
        var tt = tokenizer.getTokenTypes();

        assert tt[index] == TextFormatTokenizer.OPEN_GROUP : "Expected {";
        index++;

        var size = tokenizer.getArraySizes()[arrayIndex++];
        var builder = new ArrayNode.Builder(context, size);
        while (true) {
            assert index < tt.length : "Reached EOF but found no closing group token";

            if (tt[index] == TextFormatTokenizer.CLOSE_GROUP) {
                assert size >= builder.getUsedSize() :
                        "Invalid array size. Expected: <= " + size + ", got: " + builder.getUsedSize();
                index++;
                return builder.build();
            }

            boolean isKeyValue = tt[index + 1] == TextFormatTokenizer.EQUALS;
            if (isKeyValue) {
                assert tt[index] == TextFormatTokenizer.STRING_UNQUOTED ||
                        tt[index] == TextFormatTokenizer.STRING_QUOTED : "Expected key";

                int keyIndex = slIndex;
                slIndex++;
                index += 2;

                Node result = parseNodeIfNotSimpleValue();
                if (result == null) {
                    // System.out.println("key: " + context.evaluate(keyIndex));
                    // System.out.println("val: " + context.evaluate(slIndex));

                    builder.putKeyAndScalarValue(keyIndex, slIndex);
                    index++;
                    slIndex++;
                } else {
                    // System.out.println("key: " + context.evaluate(keyIndex));
                    // System.out.println("val: " + result.toString());

                    builder.putKeyAndNodeValue(keyIndex, result);
                }

                continue;
            }

            boolean isKeyValueWithoutEquals = tt[index] == TextFormatTokenizer.STRING_UNQUOTED &&
                    tt[index + 1] == TextFormatTokenizer.OPEN_GROUP;
            if (isKeyValueWithoutEquals) {
                int keyIndex = slIndex;
                slIndex++;
                index++;
                Node result = parseNodeIfNotSimpleValue();
                assert result != null : "KeyValue without equal sign must be an array node";
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

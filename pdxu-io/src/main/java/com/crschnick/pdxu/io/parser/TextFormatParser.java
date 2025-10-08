package com.crschnick.pdxu.io.parser;

import com.crschnick.pdxu.io.node.*;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

public final class TextFormatParser {

    public static TextFormatParser text() {
        return new TextFormatParser(StandardCharsets.UTF_8, TaggedNode.ALL, s -> true);
    }

    public static TextFormatParser eu4() {
        return new TextFormatParser(
                Charset.forName("windows-1252"),
                TaggedNode.NO_TAGS,
                s -> s.equals("map_area_data"));
    }

    public static TextFormatParser ck3() {
        return new TextFormatParser(
                StandardCharsets.UTF_8,
                TaggedNode.COLORS,
                s -> false);
    }

    public static TextFormatParser vic3() {
        return new TextFormatParser(
                StandardCharsets.UTF_8,
                TaggedNode.ALL,
                s -> false);
    }

    public static TextFormatParser hoi4() {
        return new TextFormatParser(
                StandardCharsets.UTF_8,
                TaggedNode.COLORS,
                s -> false);
    }

    public static TextFormatParser stellaris() {
        return new TextFormatParser(
                StandardCharsets.UTF_8,
                TaggedNode.COLORS,
                s -> false);
    }

    public static TextFormatParser ck2() {
        return new TextFormatParser(
                isMacOs() ? StandardCharsets.UTF_8 : Charset.forName("windows-1252"),
                TaggedNode.NO_TAGS,
                s -> false);
    }

    public static TextFormatParser vic2() {
        return new TextFormatParser(
                isMacOs() ? StandardCharsets.UTF_8 : Charset.forName("windows-1252"),
                TaggedNode.NO_TAGS,
                s -> false);
    }

    private static boolean isMacOs() {
        String osName = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
        return osName.contains("mac") || osName.contains("darwin");
    }

    private final Charset charset;
    private final TaggedNode.TagType[] possibleTags;
    private final Predicate<String> keyWithoutEquals;

    private int index;
    private int slIndex;
    private int arrayIndex;
    private int lastKnownOffset;
    private TextFormatTokenizer tokenizer;
    private NodeContext context;

    public TextFormatParser(Charset charset, TaggedNode.TagType[] possibleTags, Predicate<String> keyWithoutEquals) {
        this.charset = charset;
        this.possibleTags = possibleTags;
        this.keyWithoutEquals = keyWithoutEquals;
    }

    private void reset() {
        this.index = 0;
        this.lastKnownOffset = 0;
        this.slIndex = 0;
        this.arrayIndex = 0;
        this.tokenizer = null;
        this.context = null;
    }

    private void verifyTextFormat(byte[] input) throws ParseException {
        // People still try to open zip or rar files as text files

        if (input.length >= 4 && Arrays.equals(input, 0, 4, new byte[] {0x50, 0x4B, 0x03, 0x04}, 0, 4)) {
            throw new ParseException("Input is a zip file, not a text file");
        }

        if (input.length >= 8 && Arrays.equals(input, 0, 8, new byte[] {0x52, 0x61, 0x72, 0x21, 0x1A, 0x07, 0x01, 0x00}, 0, 8)) {
            throw new ParseException("Input is a rar file, not a text file");
        }
    }

    public synchronized ArrayNode parse(Path file) throws IOException, ParseException {
        return parse(file.getFileName().toString(), Files.readAllBytes(file), 0, false);
    }

    public synchronized ArrayNode parse(String displayName, Path file, boolean strict) throws IOException, ParseException {
        return parse(displayName, Files.readAllBytes(file), 0, strict);
    }

    public synchronized ArrayNode parse(String displayName, byte[] input, int start) throws ParseException {
        return parse(displayName, input, start, false);
    }

    public synchronized ArrayNode parse(String name, byte[] input, int start, boolean strict) throws ParseException {
        try {
            verifyTextFormat(input);

            this.tokenizer = new TextFormatTokenizer(name, input, start, strict);

            // var now = Instant.now();
            this.tokenizer.tokenize();
            // System.out.println("Tokenizer took " + ChronoUnit.MILLIS.between(now, Instant.now()) + "ms");

            this.context = new NodeContext(input, charset,
                    tokenizer.getScalarsStart(),
                    tokenizer.getScalarsLength(),
                    tokenizer.getScalarCount());

            // now = Instant.now();
            ArrayNode r = parseArray(name, strict);
            // System.out.println("Node creator took " + ChronoUnit.MILLIS.between(now, Instant.now()) + "ms");

            return r;
        } catch (ParseException ex) {
            throw ex;
        }  catch (Throwable t) {
            // Catch also errors!

            // Special case for out of memory
            if (t instanceof OutOfMemoryError ooe) {
                long heapSize = Runtime.getRuntime().totalMemory();
                long heapMaxSize = Runtime.getRuntime().maxMemory();
                long heapFreeSize = Runtime.getRuntime().freeMemory();
                var m = "" + (heapSize / 1_000_000) + "Mb / " + (heapMaxSize / 1_000_000) + "Mb (" + (heapFreeSize / 1_000_000) + "Mb free)";
                throw new ParseException("Not enough free RAM available to load file " + name + " with size " + (input.length / 1_000_000) + "Mb. " + m, ooe);
            }

            throw new ParseException(t);
        } finally {
            // Always reset!
            reset();
        }
    }

    private void updateLastKnownOffset() {
        this.lastKnownOffset = context.getLiteralsBegin()[slIndex] + context.getLiteralsLength()[slIndex];
    }

    private Node parseNodeIfNotScalarValue(String name, boolean strict) throws ParseException {
        var tt = tokenizer.getTokenTypes();
        if (tt[index] == TextFormatTokenizer.STRING_UNQUOTED) {
            var colorType = tt[index + 1] == TextFormatTokenizer.OPEN_GROUP ?
                    TaggedNode.getTagType(possibleTags, context, slIndex) : null;

            if (colorType != null) {
                if (tt[index + 1] != TextFormatTokenizer.OPEN_GROUP) {
                    throw ParseException.createFromLiteralIndex(name, "Expected {", slIndex, context);
                }

                // Move over color id
                index++;
                moveToNextScalar();

                // Move over opening {
                index++;

                List<ValueNode> components = new ArrayList<>();
                while (tt[index] != TextFormatTokenizer.CLOSE_GROUP) {
                    components.add(new ValueNode(context, slIndex));
                    moveToNextScalar();
                    index++;
                }

                // Move over closing }
                index++;

                // A color is also an array, so we have to move the array index!
                arrayIndex++;

                return new TaggedNode(colorType, components);
            }
        } else {
            if (tt[index] == TextFormatTokenizer.EQUALS) {
                throw ParseException.createFromLiteralIndex(name, "encountered unexpected =", slIndex - 1, context);
            }
            if (tt[index] == TextFormatTokenizer.CLOSE_GROUP) {
                throw ParseException.createFromLiteralIndex(name, "encountered unexpected }", slIndex - 1, context);
            }
            if (tt[index] == TextFormatTokenizer.OPEN_GROUP) {
                return parseArray(name, strict);
            }
        }

        return null;
    }

    private void moveToNextScalar() {
        slIndex++;
        updateLastKnownOffset();
    }

    private void skipOverNextNode(String name, boolean strict) throws ParseException {
        var res = parseNodeIfNotScalarValue(name, strict);

        // Node is a scalar, therefore move manually
        if (res == null) {
            index++;
            slIndex++;
        }
    }

    private ArrayNode parseArray(String name, boolean strict) throws ParseException {
        var tt = tokenizer.getTokenTypes();

        assert tt[index] == TextFormatTokenizer.OPEN_GROUP : "Expected {";
        index++;

        var size = tokenizer.getArraySizes()[arrayIndex++];
        var builder = new ArrayNode.Builder(context, size);
        while (true) {
            assert index < tt.length : "Reached EOF but found no closing group token";

            // Check for missing keys (only in non-strict mode)
            boolean isMissingKey = tt[index] == TextFormatTokenizer.EQUALS;
            if (!strict && isMissingKey) {
                // Move over =
                index++;

                // Discard next node if there is one!
                if (tt[index] != TextFormatTokenizer.CLOSE_GROUP) {
                    skipOverNextNode(name, strict);
                }
            }

            if (tt[index] == TextFormatTokenizer.CLOSE_GROUP) {
                assert size >= builder.getUsedSize() :
                        "Invalid array size. Expected: <= " + size + ", got: " + builder.getUsedSize();
                index++;

                boolean isMultiKeyValue = tt[index] == TextFormatTokenizer.EQUALS && tt[index + 1] == TextFormatTokenizer.OPEN_GROUP;
                if (isMultiKeyValue) {
                    index++;
                    var keys = parseNodeIfNotScalarValue(name, strict);
                    if (!(keys instanceof SimpleArrayNode ar) || ar.getKeyScalars() != null) {
                        throw ParseException.createFromLiteralIndex(name, "Invalid multi key", index, context);
                    }

                    var value = builder.build();
                    return new MultiKeyValueNode(context, ar.getValueScalars(), value);
                }

                return builder.build();
            }

            boolean isKeyValue = tt[index + 1] == TextFormatTokenizer.EQUALS;
            if (isKeyValue) {
                if (tt[index] != TextFormatTokenizer.STRING_UNQUOTED &&
                        tt[index] != TextFormatTokenizer.STRING_QUOTED) {
                    throw ParseException.createFromOffset(name, "Expected key", lastKnownOffset, context.getData());
                }

                int keyIndex = slIndex;
                moveToNextScalar();
                index += 2;

                Node result = parseNodeIfNotScalarValue(name, strict);
                if (result == null) {
                    // System.out.println("key: " + context.evaluate(keyIndex));
                    // System.out.println("val: " + context.evaluate(slIndex));

                    builder.putKeyAndScalarValue(keyIndex, slIndex);
                    index++;
                    moveToNextScalar();
                } else {
                    // System.out.println("key: " + context.evaluate(keyIndex));
                    // System.out.println("val: " + result.toString());

                    builder.putKeyAndNodeValue(keyIndex, result);
                }

                continue;
            }

            boolean isKeyValueWithoutEquals = tt[index] == TextFormatTokenizer.STRING_UNQUOTED &&
                    tt[index + 1] == TextFormatTokenizer.OPEN_GROUP;
            if (isKeyValueWithoutEquals && keyWithoutEquals.test(context.evaluate(slIndex))) {
                int keyIndex = slIndex;
                moveToNextScalar();
                index++;
                Node result = parseNodeIfNotScalarValue(name, strict);
                assert result != null : "KeyValue without equal sign must be an array node";
                builder.putKeyAndNodeValue(keyIndex, result);

                continue;
            }

            // Parse unnamed array element
            Node result = parseNodeIfNotScalarValue(name, strict);
            if (result == null) {
                builder.putScalarValue(slIndex);
                index++;
                moveToNextScalar();
            } else {
                builder.putNodeValue(result);
            }
        }
    }

    public Charset getCharset() {
        return charset;
    }
}

package com.crschnick.pdx_unlimiter.core.parser;

import com.crschnick.pdx_unlimiter.core.format.Namespace;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BinaryFormatParser extends FormatParser {

    public static final byte[] EQUALS = new byte[]{1, 0};
    public static final byte[] OPEN_GROUP = new byte[]{3, 0};
    public static final byte[] CLOSE_GROUP = new byte[]{4, 0};
    public static final byte[] INTEGER = new byte[]{0x0c, 0};
    public static final byte[] INTEGER_UNSIGNED = new byte[]{0x14, 0};
    public static final byte[] LONG = new byte[]{(byte) 0x9C, 0x02};
    public static final byte[] STRING_1 = new byte[]{0x0F, 0};
    public static final byte[] STRING_2 = new byte[]{0x17, 0};
    public static final byte[] FLOAT = new byte[]{0x0D, 0};
    public static final byte[] DOUBLE = new byte[]{0x67, 0x01};
    public static final byte[] BOOL = new byte[]{0x0e, 0x00};
    public static final byte[] BOOL_TRUE = new byte[]{0x4b, 0x28};
    public static final byte[] BOOL_FALSE = new byte[]{0x4c, 0x28};

    public static final byte[] EU4_MAGIC = new byte[]{0x45, 0x55, 0x34, 0x62, 0x69, 0x6E};
    public static final byte[] HOI_MAGIC = new byte[]{0x48, 0x4F, 0x49, 0x34, 0x62, 0x69, 0x6E};

    private BinaryFormatParser(byte[] magic, Namespace ns) {
        super(magic, ns);
    }

    public static BinaryFormatParser ck3Parser() {
        return new BinaryFormatParser(new byte[]{}, Namespace.CK3);
    }

    public static BinaryFormatParser eu4Parser(Namespace ns) {
        return new BinaryFormatParser(EU4_MAGIC, ns);
    }

    public static BinaryFormatParser hoi4Parser() {
        return new BinaryFormatParser(HOI_MAGIC, Namespace.HOI4);
    }

    @Override
    public List<Token> tokenize(InputStream stream) throws IOException {
        byte[] bytes = stream.readAllBytes();
        int current = 0;
        int lastSleep = 0;
        int currentToken = 0;
        List<Token> tokens = new ArrayList<>();
        do {
            byte[] next = Arrays.copyOfRange(bytes, current, current + 2);
            current += 2;

            if (Arrays.equals(next, EQUALS)) {
                tokens.add(new EqualsToken());
            } else if (Arrays.equals(next, STRING_1) || Arrays.equals(next, STRING_2)) {
                byte[] length = new byte[4];
                System.arraycopy(Arrays.copyOfRange(bytes, current, current + 2), 0, length, 0, 2);
                int lengthInt = ByteBuffer.wrap(length).order(ByteOrder.LITTLE_ENDIAN).getInt();
                current += 2;

                byte[] sb = Arrays.copyOfRange(bytes, current, current + lengthInt);
                current += lengthInt;

                if (lengthInt > 0 && sb[lengthInt - 1] == '\n') {
                    lengthInt--;
                }
                String string = new String(sb, 0, lengthInt);
                if (string.contains("Hadim Seha")) {
                    int b = 0;
                }
                tokens.add(new ValueToken(string));
            } else if (Arrays.equals(next, INTEGER)) {
                byte[] number = Arrays.copyOfRange(bytes, current, current + 4);
                current += 4;

                int numberInt = ByteBuffer.wrap(number).order(ByteOrder.LITTLE_ENDIAN).getInt();
                tokens.add(new ValueToken((long) numberInt));
            } else if (Arrays.equals(next, INTEGER_UNSIGNED)) {
                byte[] number = new byte[8];
                System.arraycopy(Arrays.copyOfRange(bytes, current, current + 4), 0, number, 0, 4);
                current += 4;

                long numberInt = ByteBuffer.wrap(number).order(ByteOrder.LITTLE_ENDIAN).getLong();
                tokens.add(new ValueToken(numberInt));
            } else if (Arrays.equals(next, LONG)) {
                byte[] number = new byte[8];
                System.arraycopy(Arrays.copyOfRange(bytes, current, current + 8), 0, number, 0, 8);
                current += 8;

                long numberInt = ByteBuffer.wrap(number).order(ByteOrder.LITTLE_ENDIAN).getLong();
                tokens.add(new ValueToken(numberInt));
            } else if (Arrays.equals(next, BOOL_TRUE)) {
                tokens.add(new ValueToken("yes"));
            } else if (Arrays.equals(next, BOOL_FALSE)) {
                tokens.add(new ValueToken("no"));
            } else if (Arrays.equals(next, BOOL)) {
                byte[] number = Arrays.copyOfRange(bytes, current, current + 1);
                current += 1;

                boolean b = ByteBuffer.wrap(number).get() != 0;
                tokens.add(new ValueToken(b ? "yes" : "no"));
            } else if (Arrays.equals(next, DOUBLE)) {
                byte[] number = Arrays.copyOfRange(bytes, current, current + 8);
                current += 8;

                long numberInt = ByteBuffer.wrap(number).order(ByteOrder.LITTLE_ENDIAN).getInt();
                double value = (2 * (numberInt * Math.pow(2, -16)));
                tokens.add(new ValueToken(value));
            } else if (Arrays.equals(next, FLOAT)) {
                byte[] number = Arrays.copyOfRange(bytes, current, current + 4);
                current += 4;

                int numberInt = ByteBuffer.wrap(number).order(ByteOrder.LITTLE_ENDIAN).getInt();
                double d = ((double) numberInt) / 1000.0;
                tokens.add(new ValueToken(d));
            } else if (Arrays.equals(next, OPEN_GROUP)) {
                tokens.add(new OpenGroupToken());
            } else if (Arrays.equals(next, CLOSE_GROUP)) {
                tokens.add(new CloseGroupToken());
            } else {
                byte[] number = new byte[4];
                System.arraycopy(next, 0, number, 0, 2);

                int numberInt = ByteBuffer.wrap(number).order(ByteOrder.LITTLE_ENDIAN).getInt();
                String id = Integer.toString(numberInt);
                tokens.add(new ValueToken(id));
            }

            currentToken++;
            if (currentToken - lastSleep > TOKEN_SLEEP_INTERVAL) {
                try {
                    Thread.sleep(SLEEP_TIME);
                } catch (InterruptedException e) {
                }
                lastSleep = currentToken;
            }

        } while (current < bytes.length);
        return tokens;
    }
}

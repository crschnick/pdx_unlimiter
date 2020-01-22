package com.paradox_challenges.eu4_unlimiter.parser.eu4;

import com.paradox_challenges.eu4_unlimiter.format.Namespace;
import com.paradox_challenges.eu4_unlimiter.parser.GamedataParser;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Eu4IronmanParser extends GamedataParser {

    public static final byte[] EQUALS = new byte[]{1, 0};
    public static final byte[] OPEN_GROUP = new byte[]{3, 0};
    public static final byte[] CLOSE_GROUP = new byte[]{4, 0};
    public static final byte[] INTEGER_1 = new byte[]{0x0c, 0};
    public static final byte[] INTEGER_2 = new byte[]{0x14, 0};
    public static final byte[] STRING_1 = new byte[]{0x0F, 0};
    public static final byte[] STRING_2 = new byte[]{0x17, 0};
    public static final byte[] FLOAT_1 = new byte[]{0x0D, 0};
    public static final byte[] FLOAT_2 = new byte[]{0x67, 0x01};
    public static final byte[] BOOL = new byte[]{0x0e, 0x00};
    public static final byte[] BOOL_TRUE = new byte[]{0x4b, 0x28};
    public static final byte[] BOOL_FALSE = new byte[]{0x4c, 0x28};
    public static final byte[] MAGIC = new byte[]{ 0x45, 0x55, 0x34, 0x62, 0x69, 0x6E};

    public Eu4IronmanParser() {
        super(MAGIC, Namespace.EU4);
    }

    @Override
    public List<Token> tokenize(InputStream stream) throws IOException {
        List<Token> tokens = new ArrayList<>();
        try {
            do {
                byte[] next = new byte[2];
                int read = stream.readNBytes(next, 0, 2);
                if (read < 2) {
                    break;
                }
                if (Arrays.equals(next, EQUALS)) {
                    tokens.add(new EqualsToken());
                }

                else if (Arrays.equals(next, STRING_1) || Arrays.equals(next, STRING_2)) {
                    byte[] length = new byte[4];
                    stream.readNBytes(length, 0, 2);
                    int lengthInt = ByteBuffer.wrap(length).order(ByteOrder.LITTLE_ENDIAN).getInt();

                    byte[] s = new byte[lengthInt];
                    stream.readNBytes(s, 0, lengthInt);
                    if (lengthInt > 0 && s[lengthInt - 1] == '\n') {
                        lengthInt--;
                    }
                    tokens.add(new ValueToken<String>(new String(s, 0, lengthInt)));
                }

                else if (Arrays.equals(next, INTEGER_1) || Arrays.equals(next, INTEGER_2)) {
                    byte[] number = new byte[4];
                    stream.readNBytes(number, 0, 4);
                    int numberInt  = ByteBuffer.wrap(number).order(ByteOrder.LITTLE_ENDIAN).getInt();
                    tokens.add(new ValueToken<Integer>(numberInt));
                }

                else if (Arrays.equals(next, BOOL_TRUE)) {
                    tokens.add(new ValueToken<Boolean>(true));
                }

                else if (Arrays.equals(next, BOOL_FALSE)) {
                    tokens.add(new ValueToken<Boolean>(false));
                }

                else if (Arrays.equals(next, BOOL)) {
                    byte[] number = new byte[1];
                    stream.readNBytes(number, 0, 1);
                    boolean b = ByteBuffer.wrap(number).get() != 0;
                    tokens.add(new ValueToken<Boolean>(b));
                }

                else if (Arrays.equals(next, FLOAT_1)) {
                    byte[] number = new byte[4];
                    stream.readNBytes(number, 0, 4);
                    int numberInt  = ByteBuffer.wrap(number).order(ByteOrder.LITTLE_ENDIAN).getInt();
                    float f = numberInt / 1000F;
                    tokens.add(new ValueToken<Float>(f));
                }

                else if (Arrays.equals(next, FLOAT_2)) {
                    byte[] number = new byte[4];
                    stream.readNBytes(number, 0, 4);
                    float f  = ByteBuffer.wrap(number).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                    f *= 2;
                    tokens.add(new ValueToken<Float>(f));
                }

                else if (Arrays.equals(next, OPEN_GROUP)) {
                    tokens.add(new OpenGroupToken());
                }

                else if (Arrays.equals(next, CLOSE_GROUP)) {
                    tokens.add(new CloseGroupToken());
                }
                else {
                    byte[] number = new byte[4];
                    number[0] = next[0];
                    number[1] = next[1];
                    int numberInt  = ByteBuffer.wrap(number).order(ByteOrder.LITTLE_ENDIAN).getInt();
                    String id = Integer.toString(numberInt);
                    tokens.add(new ValueToken<String>(id));
                }

            } while (true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tokens;
    }
}

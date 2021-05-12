package com.crschnick.pdxu.io.savegame;

import java.util.Random;

/**
 * CK3 header format:
 * <p>
 * SAV010 <type> <8 hex digits of randomness> <8 hex digits of meta data size>
 * <p>
 * type:
 * 3: Compressed + Binary
 * 2: Compressed + Plaintext
 * 1: Uncompressed + Binary
 * 0: Uncompressed + Plaintext
 * <p>
 * meta data size:
 * Length of meta data block at the beginning of the file in bytes.
 */
public record Ck3Header(boolean compressed, boolean binary, long randomness, long metaLength) {

    public static final int LENGTH = 23;

    public Ck3Header(boolean compressed, boolean binary, int metaLength) {
        this(compressed, binary, (new Random().nextLong() >>> 1) % 0xFFFFFFFFL + 1, metaLength);
    }

    public static Ck3Header fromStartOfFile(byte[] data) {
        if (data.length < LENGTH) {
            throw new IllegalArgumentException();
        }

        return fromString(new String(data, 0, LENGTH));
    }


    public static Ck3Header fromString(String header) {
        if (!header.startsWith("SAV010")) {
            throw new IllegalArgumentException();
        }

        int type = Integer.parseInt(header.substring(6, 7));
        boolean compressed = (type & 2) != 0;
        boolean binary = (type & 1) != 0;
        long randomness = Long.parseLong(header.substring(7, 15).toUpperCase(), 16);
        long metaLength = Long.parseLong(header.substring(15, 23).toUpperCase(), 16);
        return new Ck3Header(compressed, binary, randomness, metaLength);
    }

    @Override
    public String toString() {
        int type = (compressed ? 2 : 0) + (binary ? 1 : 0);
        return "SAV010" + type + String.format("%08X", randomness) + String.format("%08X", metaLength);
    }
}

package com.crschnick.pdxu.io.savegame;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Random;

/**
 * CK3 / VIC3 header format:
 * <p>
 * SAV0 <unknown> 0 <type> <8 hex digits of randomness> <8 hex digits of meta data size>
 * <p>
 * unknown:
 * Can either be 0 or 1, don't know the meaning yet
 * type:
 * 5: Split Compressed + Binary
 * 4: Split Compressed + Plaintext
 * 3: Unified Compressed + Binary
 * 2: Unified Compressed + Plaintext
 * 1: Uncompressed + Binary
 * 0: Uncompressed + Plaintext
 * <p>
 * meta data size:
 * Length of meta data block at the beginning of the file in bytes, or alternatively the amount of bytes to skip until gamestate data is read.
 * If the meta data is contained in a separate file, i.e. not embedded into the gamestate file, this value will be zero.
 */
public record ModernHeader(boolean unknown, int compressionType, boolean binary, long randomness, long metaLength) {

    public boolean isCompressed() {
        return compressionType > 0;
    }

    public boolean isUnifiedCompressed() {
        return compressionType == 1;
    }

    public boolean isSplitCompressed() {
        return compressionType == 2;
    }

    public static final int LENGTH = 23;

    public ModernHeader(boolean unknown, int compressionType, boolean binary, int metaLength) {
        this(unknown, compressionType, binary, (new Random().nextLong() >>> 1) % 0xFFFFFFFFL + 1, metaLength);
    }

    public static boolean skipsHeader(byte[] input) {
        if (input.length < LENGTH) {
            return true;
        }

        return Arrays.equals(input, 0, 9,
                             "meta_data".getBytes(StandardCharsets.UTF_8), 0, 9
        );
    }


    public static ModernHeader determineHeaderForFile(byte[] data) {
        if (data.length < LENGTH) {
            throw new SavegameFormatException("File is too short");
        }

        if (Arrays.equals(data, 0, 4, ModernHeaderCompressedSavegameStructure.ZIP_HEADER, 0, 4)) {
            throw new SavegameFormatException("Missing Header. File is just a .zip file");
        }

        return fromString(new String(data, 0, LENGTH));
    }


    public static ModernHeader fromString(String header) {
        if (!header.startsWith("SAV000") && !header.startsWith("SAV010")) {
            throw new SavegameFormatException("Invalid header start: " + header.substring(0, Math.min(6, header.length())));
        }

        boolean unknown = Integer.parseInt(header.substring(4, 5)) == 1;
        int type = Integer.parseInt(header.substring(6, 7));
        int compressedType = (type / 2);
        boolean binary = (type % 2) != 0;
        long randomness = Long.parseLong(header.substring(7, 15).toUpperCase(), 16);
        long metaLength = Long.parseLong(header.substring(15, 23).toUpperCase(), 16);
        return new ModernHeader(unknown, compressedType, binary, randomness, metaLength);
    }

    @Override
    public String toString() {
        int type = (compressionType * 2) + (binary ? 1 : 0);
        return "SAV0" + (unknown ? 1 : 0) + "0" + type + String.format("%08x", randomness) + String.format("%08x", metaLength);
    }
}

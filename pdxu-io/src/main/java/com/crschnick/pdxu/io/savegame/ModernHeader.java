package com.crschnick.pdxu.io.savegame;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Random;

/**
 * CK3, VIC3, EU5 header format:
 * <p>
 * SAV <version> <type> <8 hex digits of randomness> <8 hex digits of meta data size> [<8 hex digits of padding>]
 * <p>
 * version:
 * Can either be 00, 01, 02
 * type:
 * 05: Split Compressed + Binary
 * 04: Split Compressed + Plaintext
 * 03: Unified Compressed + Binary
 * 02: Unified Compressed + Plaintext
 * 01: Uncompressed + Binary
 * 00: Uncompressed + Plaintext
 * <p>
 * meta data size:
 * Length of meta data block at the beginning of the file in bytes, or alternatively the amount of bytes to skip until gamestate data is read.
 * If the meta data is contained in a separate file, i.e. not embedded into the gamestate file, this value will be zero.
 * padding:
 * in v2, there is padding at the end. In v1, there is no padding
 */
public record ModernHeader(int version, int compressionType, boolean binary, long randomness, long metaLength) {

    public boolean isCompressed() {
        return compressionType > 0;
    }

    public boolean isUnifiedCompressed() {
        return compressionType == 1;
    }

    public boolean isSplitCompressed() {
        return compressionType == 2;
    }

    public static final int V1_LENGTH = 23;
    public static final int V2_LENGTH = V1_LENGTH + 8;

    public ModernHeader(int version, int compressionType, boolean binary, int metaLength) {
        this(version, compressionType, binary, (new Random().nextLong() >>> 1) % 0xFFFFFFFFL + 1, metaLength);
    }

    public static boolean skipsHeader(byte[] input) {
        if (input.length < V1_LENGTH) {
            return true;
        }

        return Arrays.equals(input, 0, 9, "meta_data".getBytes(StandardCharsets.UTF_8), 0, 9)
                || Arrays.equals(input, 0, 8, "metadata".getBytes(StandardCharsets.UTF_8), 0, 8);
    }

    public static ModernHeader determineHeaderForFile(byte[] data) {
        if (data.length < 5) {
            throw new SavegameFormatException("File is too short");
        }

        if (Arrays.equals(data, 0, 4, ModernHeaderCompressedSavegameStructure.ZIP_HEADER, 0, 4)) {
            throw new SavegameFormatException("Missing Header. File is just a .zip file");
        }

        var start = new String(data, 0, 3);
        if (!start.equals("SAV")) {
            throw new SavegameFormatException("Invalid header start: " + start);
        }

        var version = Integer.parseInt(new String(data, 4, 1));
        var v2 = version == 2;
        if ((v2 && data.length < V2_LENGTH) || (!v2 && data.length < V1_LENGTH)) {
            throw new SavegameFormatException("File is too short");
        }

        return fromString(new String(data, 0, v2 ? V2_LENGTH : V1_LENGTH));
    }

    public static ModernHeader fromString(String header) {
        if (!header.startsWith("SAV000") && !header.startsWith("SAV010") && !header.startsWith("SAV020")) {
            throw new SavegameFormatException(
                    "Invalid header start: " + header.substring(0, Math.min(6, header.length())));
        }

        int version = Integer.parseInt(header.substring(4, 5));
        int type = Integer.parseInt(header.substring(6, 7));
        int compressedType = (type / 2);
        boolean binary = (type % 2) != 0;
        long randomness = Long.parseLong(header.substring(7, 15).toUpperCase(), 16);
        long metaLength = Long.parseLong(header.substring(15, 23).toUpperCase(), 16);

        if (version == 2) {
            long padding = Long.parseLong(header.substring(23, 31).toUpperCase(), 16);
            if (padding != 0) {
                throw new SavegameFormatException("Invalid header padding: " + padding);
            }
        }

        return new ModernHeader(version, compressedType, binary, randomness, metaLength);
    }

    @Override
    public String toString() {
        int type = (compressionType * 2) + (binary ? 1 : 0);
        return "SAV0" + version + "0" + type + String.format("%08x", randomness) + String.format("%08x", metaLength)
                + (version == 2 ? "0".repeat(8) : "");
    }
}

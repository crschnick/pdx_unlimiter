package com.crschnick.pdxu.io.savegame;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Locale;

final class JominiChecksum {

    private static final int CHECKSUM_OFFSET = 7;
    private static final int CHECKSUM_LENGTH = 8;
    private static final int HASHED_FILE_OFFSET = CHECKSUM_OFFSET + CHECKSUM_LENGTH;

    private JominiChecksum() {}

    static void writeExport(Path source, Path target, long steamUserId) throws IOException {
        try (var input = FileChannel.open(source, StandardOpenOption.READ);
                var output = FileChannel.open(target, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            long fileSize = input.size();
            validateHeader(input);
            if (fileSize > 0xFFFF_FFFFL) {
                throw new SavegameFormatException("Savegame is too large for a Jomini checksum");
            }

            var hash = new PMurHash32();
            hash.update(ChecksumIO.littleEndianLong(steamUserId));
            hash.update(ChecksumIO.littleEndianInt((int) fileSize));

            var header = ByteBuffer.allocate(HASHED_FILE_OFFSET);
            input.position(0);
            ChecksumIO.readFully(input, header);
            header.flip();
            ChecksumIO.writeFully(output, header);

            var buffer = ByteBuffer.allocate(ChecksumIO.BUFFER_SIZE);
            int read;
            while ((read = input.read(buffer)) != -1) {
                if (read != 0) {
                    hash.update(buffer.array(), 0, read);
                    buffer.flip();
                    ChecksumIO.writeFully(output, buffer);
                    buffer.clear();
                }
            }
            hash.update(new byte[] {'P', 'D', 'X'});

            var encoded = String.format(Locale.ROOT, "%08x", hash.finish()).getBytes(StandardCharsets.US_ASCII);
            output.position(CHECKSUM_OFFSET);
            ChecksumIO.writeFully(output, ByteBuffer.wrap(encoded));
        }
    }

    private static void validateHeader(FileChannel input) throws IOException {
        if (input.size() < JominiHeader.V1_LENGTH + 1L) {
            throw new SavegameFormatException("File is too short");
        }
        var magic = ByteBuffer.allocate(3);
        input.position(0);
        ChecksumIO.readFully(input, magic);
        if (!Arrays.equals(magic.array(), new byte[] {'S', 'A', 'V'})) {
            throw new SavegameFormatException("Invalid Jomini savegame header");
        }
    }

    private static final class PMurHash32 {

        private static final int MIX_1 = 0xCC9E2D51;
        private static final int MIX_2 = 0x1B873593;

        private int hash;
        private int tail;
        private int tailLength;
        private long length;

        private void update(byte[] input) {
            update(input, 0, input.length);
        }

        private void update(byte[] input, int offset, int inputLength) {
            length += inputLength;
            int index = offset;
            int remaining = inputLength;
            while (tailLength != 0 && remaining != 0) {
                tail |= (input[index++] & 0xFF) << (tailLength++ * 8);
                remaining--;
                if (tailLength == Integer.BYTES) {
                    mixBlock(tail);
                    tail = 0;
                    tailLength = 0;
                }
            }
            while (remaining >= Integer.BYTES) {
                int block = (input[index] & 0xFF)
                        | ((input[index + 1] & 0xFF) << 8)
                        | ((input[index + 2] & 0xFF) << 16)
                        | ((input[index + 3] & 0xFF) << 24);
                mixBlock(block);
                index += Integer.BYTES;
                remaining -= Integer.BYTES;
            }
            while (remaining-- != 0) {
                tail |= (input[index++] & 0xFF) << (tailLength++ * 8);
            }
        }

        private int finish() {
            int result = hash;
            if (tailLength != 0) {
                int mixedTail = Integer.rotateLeft(tail * MIX_1, 15) * MIX_2;
                result ^= mixedTail;
            }
            result ^= (int) length;
            result ^= result >>> 16;
            result *= 0x85EBCA6B;
            result ^= result >>> 13;
            result *= 0xC2B2AE35;
            result ^= result >>> 16;
            return result;
        }

        private void mixBlock(int block) {
            block = Integer.rotateLeft(block * MIX_1, 15) * MIX_2;
            hash ^= block;
            hash = Integer.rotateLeft(hash, 13) * 5 + 0xE6546B64;
        }
    }
}

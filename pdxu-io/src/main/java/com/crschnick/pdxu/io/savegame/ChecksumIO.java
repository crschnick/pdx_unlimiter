package com.crschnick.pdxu.io.savegame;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

final class ChecksumIO {

    static final int BUFFER_SIZE = 1024 * 1024;

    private ChecksumIO() {}

    static void readFully(SeekableByteChannel channel, ByteBuffer buffer) throws IOException {
        while (buffer.hasRemaining()) {
            if (channel.read(buffer) == -1) {
                throw new SavegameFormatException("Unexpected end of savegame");
            }
        }
    }

    static void writeFully(SeekableByteChannel channel, ByteBuffer buffer) throws IOException {
        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }
    }

    static byte[] littleEndianInt(int value) {
        return new byte[] {(byte) value, (byte) (value >>> 8), (byte) (value >>> 16), (byte) (value >>> 24)};
    }

    static byte[] littleEndianLong(long value) {
        return new byte[] {
            (byte) value,
            (byte) (value >>> 8),
            (byte) (value >>> 16),
            (byte) (value >>> 24),
            (byte) (value >>> 32),
            (byte) (value >>> 40),
            (byte) (value >>> 48),
            (byte) (value >>> 56)
        };
    }
}

package com.crschnick.pdxu.io.savegame;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HexFormat;

final class PreJominiChecksum {

    private static final int CHECKSUM_LENGTH = 32;
    private static final byte[] ZIP_MAGIC = {0x50, 0x4B, 0x03, 0x04};

    private PreJominiChecksum() {}

    static void writeExport(Path source, Path target, long steamUserId, String gameId) throws IOException {
        if (!isZip(source)) {
            if (!writePart(source, target, steamUserId, gameId)) {
                throw new SavegameFormatException("Invalid pre-Jomini savegame header");
            }
            return;
        }

        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        boolean repaired = false;
        try (var fileSystem = FileSystems.newFileSystem(target);
                var paths = Files.list(fileSystem.getPath("/"))) {
            for (var path : paths.filter(Files::isRegularFile).toList()) {
                repaired |= repairPart(path, steamUserId, gameId);
            }
        }
        if (!repaired) {
            throw new SavegameFormatException("Archive contains no pre-Jomini savegame parts");
        }
    }

    private static boolean repairPart(Path path, long steamUserId, String gameId) throws IOException {
        var header = readHeader(path, gameId);
        if (header == null) {
            return false;
        }

        try (var channel = FileChannel.open(path, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
            trimTrailingNewline(channel);
            var encoded = calculate(channel, header, steamUserId).getBytes(StandardCharsets.US_ASCII);
            channel.position(channel.size() - header.checksumDistanceFromEnd());
            ChecksumIO.writeFully(channel, ByteBuffer.wrap(encoded));
        }
        return true;
    }

    private static boolean writePart(Path source, Path target, long steamUserId, String gameId) throws IOException {
        var header = readHeader(source, gameId);
        if (header == null) {
            return false;
        }

        try (var input = FileChannel.open(source, StandardOpenOption.READ);
                var output = FileChannel.open(target, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            long exportSize = input.size();
            if (exportSize != 0 && readByte(input, exportSize - 1) == '\n') {
                exportSize--;
            }

            long contentLength = exportSize - header.footerLength();
            if (contentLength < 0) {
                throw new SavegameFormatException("Pre-Jomini savegame is too short");
            }
            validateFooter(input, header, exportSize);

            var fletcher = new Fletcher32();
            var buffer = ByteBuffer.allocate(ChecksumIO.BUFFER_SIZE);
            input.position(0);
            long remaining = contentLength;
            while (remaining != 0) {
                buffer.clear();
                buffer.limit((int) Math.min(buffer.capacity(), remaining));
                int read = input.read(buffer);
                if (read == -1) {
                    throw new SavegameFormatException("Unexpected end of pre-Jomini savegame");
                }
                if (read != 0) {
                    fletcher.update(buffer.array(), 0, read);
                    remaining -= read;
                    buffer.flip();
                    ChecksumIO.writeFully(output, buffer);
                }
            }

            var footer = ByteBuffer.allocate(header.footerLength());
            input.position(contentLength);
            ChecksumIO.readFully(input, footer);
            var encoded = finishChecksum(fletcher, contentLength, steamUserId).getBytes(StandardCharsets.US_ASCII);
            int checksumOffset = header.footerLength() - header.checksumDistanceFromEnd();
            System.arraycopy(encoded, 0, footer.array(), checksumOffset, encoded.length);
            footer.flip();
            ChecksumIO.writeFully(output, footer);
        }
        return true;
    }

    private static PreJominiHeader readHeader(Path path, String gameId) throws IOException {
        int headerLength = gameId.length() + 3;
        try (var input = Files.newInputStream(path)) {
            return PreJominiHeader.determine(input.readNBytes(headerLength), gameId);
        }
    }

    private static String calculate(FileChannel channel, PreJominiHeader header, long steamUserId) throws IOException {
        long fileSize = channel.size();
        long contentLength = fileSize - header.footerLength();
        if (contentLength < 0) {
            throw new SavegameFormatException("Pre-Jomini savegame is too short");
        }
        validateFooter(channel, header, fileSize);

        var fletcher = new Fletcher32();
        var buffer = ByteBuffer.allocate(ChecksumIO.BUFFER_SIZE);
        channel.position(0);
        long remaining = contentLength;
        while (remaining != 0) {
            buffer.clear();
            buffer.limit((int) Math.min(buffer.capacity(), remaining));
            int read = channel.read(buffer);
            if (read == -1) {
                throw new SavegameFormatException("Unexpected end of pre-Jomini savegame");
            }
            if (read != 0) {
                fletcher.update(buffer.array(), 0, read);
                remaining -= read;
            }
        }

        return finishChecksum(fletcher, contentLength, steamUserId);
    }

    private static String finishChecksum(Fletcher32 fletcher, long contentLength, long steamUserId) {
        var digest = md5();
        var prefix = Integer.toString(fletcher.finish()) + Long.toString(contentLength);
        digest.update(prefix.getBytes(StandardCharsets.US_ASCII));
        digest.update(ChecksumIO.littleEndianLong(steamUserId));
        return HexFormat.of().formatHex(digest.digest());
    }

    private static void validateFooter(FileChannel channel, PreJominiHeader header, long exportSize)
            throws IOException {
        if (exportSize < header.footerLength()) {
            throw new SavegameFormatException("Pre-Jomini savegame is too short");
        }
        var checksum = ByteBuffer.allocate(CHECKSUM_LENGTH);
        channel.position(exportSize - header.checksumDistanceFromEnd());
        ChecksumIO.readFully(channel, checksum);
        for (byte value : checksum.array()) {
            boolean digit = value >= '0' && value <= '9';
            boolean lowercase = value >= 'a' && value <= 'f';
            boolean uppercase = value >= 'A' && value <= 'F';
            if (!digit && !lowercase && !uppercase) {
                throw new SavegameFormatException("Invalid pre-Jomini checksum footer");
            }
        }
        if (header == PreJominiHeader.TEXT) {
            byte openingQuote = readByte(channel, exportSize - header.footerLength());
            byte closingQuote = readByte(channel, exportSize - 1);
            if (openingQuote != '"' || closingQuote != '"') {
                throw new SavegameFormatException("Invalid pre-Jomini text checksum footer");
            }
        }
    }

    private static void trimTrailingNewline(FileChannel channel) throws IOException {
        long size = channel.size();
        if (size != 0 && readByte(channel, size - 1) == '\n') {
            channel.truncate(size - 1);
        }
    }

    private static byte readByte(FileChannel channel, long position) throws IOException {
        var value = ByteBuffer.allocate(1);
        channel.position(position);
        ChecksumIO.readFully(channel, value);
        return value.array()[0];
    }

    private static boolean isZip(Path source) throws IOException {
        try (var input = Files.newInputStream(source)) {
            return Arrays.equals(input.readNBytes(ZIP_MAGIC.length), ZIP_MAGIC);
        }
    }

    private static MessageDigest md5() {
        try {
            return MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 is unavailable", e);
        }
    }

    private static final class Fletcher32 {

        private int upper;
        private int lower;

        private void update(byte[] input, int offset, int inputLength) {
            int end = offset + inputLength;
            for (int index = offset; index < end; index++) {
                upper += input[index] & 0xFF;
                if (upper > 0xFFFF) {
                    upper -= 0xFFFF;
                }
                lower += upper;
                if (lower > 0xFFFF) {
                    lower -= 0xFFFF;
                }
            }
        }

        private int finish() {
            return (upper << 16) | lower;
        }
    }
}

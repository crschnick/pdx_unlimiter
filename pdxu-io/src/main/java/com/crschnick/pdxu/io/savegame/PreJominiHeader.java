package com.crschnick.pdxu.io.savegame;

import java.nio.charset.StandardCharsets;

public enum PreJominiHeader {
    TEXT("txt", 34, 33),
    BINARY("bin", 36, 32);

    private final String suffix;
    private final int footerLength;
    private final int checksumDistanceFromEnd;

    PreJominiHeader(String suffix, int footerLength, int checksumDistanceFromEnd) {
        this.suffix = suffix;
        this.footerLength = footerLength;
        this.checksumDistanceFromEnd = checksumDistanceFromEnd;
    }

    public static PreJominiHeader determine(byte[] header, String gameId) {
        var value = new String(header, StandardCharsets.US_ASCII);
        for (var candidate : values()) {
            if (value.equals(gameId + candidate.suffix)) {
                return candidate;
            }
        }
        return null;
    }

    public int footerLength() {
        return footerLength;
    }

    public int checksumDistanceFromEnd() {
        return checksumDistanceFromEnd;
    }
}

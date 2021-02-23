package com.crschnick.pdx_unlimiter.core.node;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class NodeConstants {

    public static final int UTF_8 = 0;
    public static final int ISO_8859_1 = 1;

    public static final int EVALUATED = 4;

    public static final int QUOTED = 8;

    public static final int TYPE_BOOLEAN = 16;
    public static final int TYPE_INTEGER = 32;
    public static final int TYPE_STRING = 64;
    public static final int TYPE_LONG = 128;
    public static final int TYPE_FLOATING_POINT = 256;

    public static Charset getCharset(int status) {
        int id = status & 0x3;
        if (id == UTF_8) {
            return StandardCharsets.UTF_8;
        }

        if (id == ISO_8859_1) {
            return StandardCharsets.ISO_8859_1;
        }

        throw new IllegalArgumentException();
    }
}

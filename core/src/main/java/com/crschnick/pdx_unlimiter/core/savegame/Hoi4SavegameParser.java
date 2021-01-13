package com.crschnick.pdx_unlimiter.core.savegame;

import com.crschnick.pdx_unlimiter.core.parser.FormatParser;
import com.crschnick.pdx_unlimiter.core.parser.Node;
import com.crschnick.pdx_unlimiter.core.parser.TextFormatParser;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;

public class Hoi4SavegameParser extends SavegameParser {

    private static final byte[] HOI_BINARY_HEADER = new byte[]{0x48, 0x4F, 0x49, 0x34, 0x62, 0x69, 0x6E};

    @Override
    public Status parse(Path input, Melter melter) {
        return null;
    }
}

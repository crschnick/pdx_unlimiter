package com.crschnick.pdx_unlimiter.core.savegame;

import com.crschnick.pdx_unlimiter.core.parser.FormatParser;
import com.crschnick.pdx_unlimiter.core.parser.Node;
import com.crschnick.pdx_unlimiter.core.parser.TextFormatParser;

import java.io.ByteArrayInputStream;

public class Hoi4SavegameParser extends SavegameParser {

    private static final byte[] HOI_BINARY_HEADER = new byte[]{0x48, 0x4F, 0x49, 0x34, 0x62, 0x69, 0x6E};

    @Override
    public boolean isBinaryFormat(byte[] content) throws Exception {
        var in = new ByteArrayInputStream(content);
        return FormatParser.validateHeader(HOI_BINARY_HEADER, in);
    }

    @Override
    public Node parse(byte[] content) throws Exception {
        var in = new ByteArrayInputStream(content);
        return TextFormatParser.hoi4SavegameParser().parse(in);
    }
}

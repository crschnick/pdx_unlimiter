package com.crschnick.pdx_unlimiter.core.savegame;

import com.crschnick.pdx_unlimiter.core.parser.ParseException;
import com.crschnick.pdx_unlimiter.core.parser.TextFormatParser;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class Ck3PlaintextSavegameStructure implements SavegameStructure {

    @Override
    public SavegameParseResult parse(byte[] input) {
        var header = Ck3Header.fromStartOfFile(input);
        if (header.binary()) {
            throw new IllegalArgumentException("Binary savegames are not supported");
        }
        if (header.compressed()) {
            throw new IllegalArgumentException("Compressed savegames are not supported");
        }

        int metaStart = header.toString().length() + 1;
        try {
            var node = new TextFormatParser(StandardCharsets.UTF_8)
                    .parse(input, metaStart);
            return new SavegameParseResult.Success(Map.of("gamestate", node), input);
        } catch (Throwable t) {
            return new SavegameParseResult.Error(t);
        }
    }
}

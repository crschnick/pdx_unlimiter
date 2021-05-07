package com.crschnick.pdx_unlimiter.core.savegame;

import com.crschnick.pdx_unlimiter.core.parser.ParseException;
import com.crschnick.pdx_unlimiter.core.parser.TextFormatParser;

import java.nio.charset.Charset;
import java.util.Map;

public record PlaintextSavegameStructure(byte[] header, Charset charset, String name) implements SavegameStructure {

    @Override
    public SavegameParseResult parse(byte[] input) {
        if (header != null && !SavegameStructure.validateHeader(header, input)) {
            return new SavegameParseResult.Invalid("File " + name + " has an invalid header");
        }

        try {
            var node = new TextFormatParser(charset).parse(input, header != null ? header.length + 1 : 0);
            return new SavegameParseResult.Success(Map.of(name, node), input);
        } catch (ParseException e) {
            return new SavegameParseResult.Error(e);
        }
    }
}

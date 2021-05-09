package com.crschnick.pdx_unlimiter.core.savegame;

import com.crschnick.pdx_unlimiter.core.node.ArrayNode;
import com.crschnick.pdx_unlimiter.core.writer.NodeWriter;
import com.crschnick.pdx_unlimiter.core.parser.ParseException;
import com.crschnick.pdx_unlimiter.core.parser.TextFormatParser;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public record PlaintextSavegameStructure(byte[] header, Charset charset, String name) implements SavegameStructure {

    @Override
    public void write(Path out, Map<String, ArrayNode> nodes) throws IOException {
        try (var partOut = Files.newOutputStream(out)) {
            NodeWriter.write(partOut, charset, nodes.values().iterator().next(), "\t");
        }
    }

    @Override
    public SavegameParseResult parse(byte[] input) {
        if (header != null && !SavegameStructure.validateHeader(header, input)) {
            return new SavegameParseResult.Invalid("File " + name + " has an invalid header");
        }

        try {
            var node = new TextFormatParser(charset).parse(input, header != null ? header.length + 1 : 0);
            return new SavegameParseResult.Success(Map.of(name, node));
        } catch (ParseException e) {
            return new SavegameParseResult.Error(e);
        }
    }

    @Override
    public Charset getCharset() {
        return charset();
    }
}

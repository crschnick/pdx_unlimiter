package com.crschnick.pdxu.io.savegame;

import com.crschnick.pdxu.io.node.ArrayNode;
import com.crschnick.pdxu.io.parser.ParseException;
import com.crschnick.pdxu.io.parser.TextFormatParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class PlaintextSavegameStructure implements SavegameStructure {

    protected final byte[] header;
    private final String name;
    private final TextFormatParser parser;

    public PlaintextSavegameStructure(byte[] header, String name, TextFormatParser parser) {
        this.header = header;
        this.name = name;
        this.parser = parser;
    }

    @Override
    public void write(Path out, Map<String, ArrayNode> nodes) throws IOException {
        try (var partOut = Files.newOutputStream(out)) {
            if (header != null) {
                partOut.write(header);
                partOut.write("\n".getBytes());
            }
            writeData(partOut, nodes.values().iterator().next());
        }
    }

    @Override
    public SavegameParseResult parse(byte[] input) {
        if (header != null && !SavegameStructure.validateHeader(header, input)) {
            return new SavegameParseResult.Invalid("File " + name + " has an invalid header");
        }

        try {
            var node = parser.parse(input, header != null ? header.length + 1 : 0);
            return new SavegameParseResult.Success(Map.of(name, node));
        } catch (ParseException e) {
            return new SavegameParseResult.Error(e);
        }
    }

    @Override
    public TextFormatParser getParser() {
        return parser;
    }
}

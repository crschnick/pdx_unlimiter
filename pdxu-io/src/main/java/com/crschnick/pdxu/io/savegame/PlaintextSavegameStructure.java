package com.crschnick.pdxu.io.savegame;

import com.crschnick.pdxu.io.node.ArrayNode;
import com.crschnick.pdxu.io.node.NodeWriter;
import com.crschnick.pdxu.io.node.TaggedNode;
import com.crschnick.pdxu.io.parser.ParseException;
import com.crschnick.pdxu.io.parser.TextFormatParser;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class PlaintextSavegameStructure implements SavegameStructure {

    private final byte[] header;
    private final Charset charset;
    private final String name;
    private final TaggedNode.TagType[] tagTypes;

    public PlaintextSavegameStructure(byte[] header, Charset charset, String name, TaggedNode.TagType[] tagTypes) {
        this.header = header;
        this.charset = charset;
        this.name = name;
        this.tagTypes = tagTypes;
    }

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
            var node = new TextFormatParser(charset, tagTypes).parse(input, header != null ? header.length + 1 : 0);
            return new SavegameParseResult.Success(Map.of(name, node));
        } catch (ParseException e) {
            return new SavegameParseResult.Error(e);
        }
    }

    @Override
    public Charset getCharset() {
        return charset;
    }
}

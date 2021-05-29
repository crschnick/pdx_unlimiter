package com.crschnick.pdxu.io.savegame;

import com.crschnick.pdxu.io.node.ArrayNode;
import com.crschnick.pdxu.io.node.NodeWriter;
import com.crschnick.pdxu.io.parser.TextFormatParser;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

public interface SavegameStructure {

    SavegameStructure EU4_PLAINTEXT = new PlaintextSavegameStructure(
            "EU4txt".getBytes(),
            "gamestate",
            TextFormatParser.EU4);

    SavegameStructure EU4_COMPRESSED = new ZipSavegameStructure(
            "EU4txt".getBytes(),
            TextFormatParser.EU4,
            Set.of(new ZipSavegameStructure.SavegamePart("ai", "ai"),
                    new ZipSavegameStructure.SavegamePart("meta", "meta"),
                    new ZipSavegameStructure.SavegamePart("gamestate", "gamestate")),
            "rnw.zip");


    SavegameStructure CK3_PLAINTEXT = new Ck3PlaintextSavegameStructure();

    SavegameStructure CK3_COMPRESSED = new Ck3CompressedSavegameStructure();


    SavegameStructure HOI4 = new PlaintextSavegameStructure(
            "HOI4txt".getBytes(),
            "gamestate",
            TextFormatParser.HOI4);


    SavegameStructure STELLARIS = new ZipSavegameStructure(
            null,
            TextFormatParser.STELLARIS,
            Set.of(new ZipSavegameStructure.SavegamePart("meta", "meta"),
                    new ZipSavegameStructure.SavegamePart("gamestate", "gamestate")));


    SavegameStructure CK2_PLAINTEXT = new PlaintextSavegameStructure(
            "CK2txt".getBytes(),
            "gamestate",
            TextFormatParser.CK2) {

        @Override
        public void writeData(OutputStream out, ArrayNode node) throws IOException {
            NodeWriter.write(out, getParser().getCharset(), node, "\t", 1);
            out.write("}".getBytes());
        }
    };

    SavegameStructure CK2_COMPRESSED = new ZipSavegameStructure(
            "CK2txt".getBytes(),
            TextFormatParser.CK2,
            Set.of(new ZipSavegameStructure.SavegamePart("meta", "meta"),
                    new ZipSavegameStructure.SavegamePart("gamestate", "*"))) {

        @Override
        public void writeData(OutputStream out, ArrayNode node) throws IOException {
            NodeWriter.write(out, getParser().getCharset(), node, "\t", 1);
            out.write("\n}".getBytes());
        }
    };

    SavegameStructure VIC2 = new PlaintextSavegameStructure(
            null,
            "gamestate",
            TextFormatParser.VIC2) {

        @Override
        public void writeData(OutputStream out, ArrayNode node) throws IOException {
            NodeWriter.write(out, getParser().getCharset(), node, "\t", 0);
            out.write("}".getBytes());
        }
    };

    static boolean validateHeader(byte[] header, byte[] content) {
        if (content.length < header.length) {
            return false;
        }

        byte[] first = new byte[header.length];
        System.arraycopy(content, 0, first, 0, header.length);
        return Arrays.equals(first, header);
    }

    void write(Path out, Map<String, ArrayNode> nodes) throws IOException;

    default void writeData(OutputStream out, ArrayNode node) throws IOException {
        NodeWriter.write(out, getParser().getCharset(), node, "\t", 0);
    }

    SavegameParseResult parse(byte[] input);

    TextFormatParser getParser();
}

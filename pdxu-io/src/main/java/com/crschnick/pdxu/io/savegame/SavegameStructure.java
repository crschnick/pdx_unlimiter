package com.crschnick.pdxu.io.savegame;

import com.crschnick.pdxu.io.node.ArrayNode;
import com.crschnick.pdxu.io.node.TaggedNode;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

public interface SavegameStructure {

    SavegameStructure EU4_PLAINTEXT = new PlaintextSavegameStructure(
            "EU4txt".getBytes(),
            StandardCharsets.ISO_8859_1,
            "gamestate",
            TaggedNode.NO_TAGS);

    SavegameStructure EU4_COMPRESSED = new ZipSavegameStructure(
            "EU4txt".getBytes(),
            StandardCharsets.ISO_8859_1,
            TaggedNode.NO_TAGS,
            Set.of(new ZipSavegameStructure.SavegamePart("ai", "ai"),
                    new ZipSavegameStructure.SavegamePart("meta", "meta"),
                    new ZipSavegameStructure.SavegamePart("gamestate", "gamestate")),
            "rnw.zip");


    SavegameStructure CK3_PLAINTEXT = new Ck3PlaintextSavegameStructure();

    SavegameStructure CK3_COMPRESSED = new Ck3CompressedSavegameStructure();


    SavegameStructure HOI4 = new PlaintextSavegameStructure(
            "HOI4txt".getBytes(),
            StandardCharsets.UTF_8,
            "gamestate",
            TaggedNode.COLORS);


    SavegameStructure STELLARIS = new ZipSavegameStructure(
            null,
            StandardCharsets.UTF_8,
            TaggedNode.COLORS,
            Set.of(new ZipSavegameStructure.SavegamePart("meta", "meta"),
                    new ZipSavegameStructure.SavegamePart("gamestate", "gamestate")));


    SavegameStructure CK2_PLAINTEXT = new PlaintextSavegameStructure(
            "CK2txt".getBytes(),
            StandardCharsets.ISO_8859_1,
            "gamestate",
            TaggedNode.NO_TAGS);

    SavegameStructure CK2_COMPRESSED = new ZipSavegameStructure(
            "CK2txt".getBytes(),
            StandardCharsets.ISO_8859_1,
            TaggedNode.NO_TAGS,
            Set.of(new ZipSavegameStructure.SavegamePart("meta", "meta"),
                    new ZipSavegameStructure.SavegamePart("gamestate", "*")));


    SavegameStructure VIC2 = new PlaintextSavegameStructure(
            null,
            StandardCharsets.UTF_8,
            "gamestate",
            TaggedNode.NO_TAGS);

    static boolean validateHeader(byte[] header, byte[] content) {
        if (content.length < header.length) {
            return false;
        }

        byte[] first = new byte[header.length];
        System.arraycopy(content, 0, first, 0, header.length);
        return Arrays.equals(first, header);
    }

    void write(Path out, Map<String, ArrayNode> nodes) throws IOException;

    SavegameParseResult parse(byte[] input);

    Charset getCharset();


}

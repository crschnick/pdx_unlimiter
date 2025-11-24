package com.crschnick.pdxu.io.savegame;

import com.crschnick.pdxu.io.node.ArrayNode;
import com.crschnick.pdxu.io.node.NodeWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Set;
import java.util.regex.Pattern;

public interface SavegameStructure {

    SavegameStructure EU4_PLAINTEXT =
            new PlaintextSavegameStructure("EU4txt".getBytes(), "gamestate", SavegameType.EU4);

    SavegameStructure EU4_COMPRESSED = new ZipSavegameStructure(
            "EU4txt".getBytes(),
            SavegameType.EU4,
            Set.of(
                    new ZipSavegameStructure.SavegamePart("ai", "ai"),
                    new ZipSavegameStructure.SavegamePart("meta", "meta"),
                    new ZipSavegameStructure.SavegamePart("gamestate", "gamestate")),
            "rnw.zip");

    SavegameStructure CK3_PLAINTEXT = new ModernPlaintextSavegameStructure(SavegameType.CK3);
    SavegameStructure CK3_COMPRESSED = new ModernHeaderCompressedSavegameStructure(SavegameType.CK3);

    SavegameStructure VIC3_PLAINTEXT = new ModernPlaintextSavegameStructure(SavegameType.VIC3);
    SavegameStructure VIC3_UNIFIED_COMPRESSED = new ModernHeaderCompressedSavegameStructure(SavegameType.VIC3);
    SavegameStructure VIC3_SPLIT_COMPRESSED = new ModernSplitCompressedSavegameStructure(SavegameType.VIC3);

    SavegameStructure HOI4 = new PlaintextSavegameStructure("HOI4txt".getBytes(), "gamestate", SavegameType.HOI4);

    SavegameStructure STELLARIS = new ZipSavegameStructure(
            null,
            SavegameType.STELLARIS,
            Set.of(
                    new ZipSavegameStructure.SavegamePart("meta", "meta"),
                    new ZipSavegameStructure.SavegamePart("gamestate", "gamestate")));

    SavegameStructure CK2_PLAINTEXT =
            new PlaintextSavegameStructure("CK2txt".getBytes(), "gamestate", SavegameType.CK2) {

                @Override
                public void writeData(OutputStream out, ArrayNode node) throws IOException {
                    NodeWriter.write(out, getType().getParser().getCharset(), node, "\t", 1);
                    out.write("}".getBytes());
                }
            };

    SavegameStructure CK2_COMPRESSED =
            new ZipSavegameStructure(
                    "CK2txt".getBytes(),
                    SavegameType.CK2,
                    Set.of(
                            new ZipSavegameStructure.SavegamePart("meta", "meta"),
                            new ZipSavegameStructure.SavegamePart("*", "gamestate"))) {

                @Override
                public void writeData(OutputStream out, ArrayNode node) throws IOException {
                    NodeWriter.write(out, getType().getParser().getCharset(), node, "\t", 1);
                    out.write("\n}".getBytes());
                }
            };

    SavegameStructure VIC2 = new PlaintextSavegameStructure(null, "gamestate", SavegameType.VIC2) {

        @Override
        public void writeData(OutputStream out, ArrayNode node) throws IOException {
            NodeWriter.write(out, getType().getParser().getCharset(), node, "\t", 0);
            out.write("}".getBytes());
        }
    };

    SavegameStructure EU5_PLAINTEXT = new ModernPlaintextSavegameStructure(SavegameType.EU5) {
        @Override
        protected int determineHeaderVersion(SavegameContent content) {
            var gamestate = content.get("gamestate");
            var version = gamestate.getNodeForKeys("metadata", "version");
            var p = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)");
            var matcher = p.matcher(version.getString());
            if (matcher.matches()) {
                var requiresV2 = Integer.parseInt(matcher.group(1)) > 1 ||
                        Integer.parseInt(matcher.group(2)) > 0 ||
                        Integer.parseInt(matcher.group(3)) >= 8;
                return requiresV2 ? 2 : 1;
            } else {
                return 1;
            }
        }
    };
    SavegameStructure EU5_COMPRESSED = new ModernHeaderCompressedSavegameStructure(SavegameType.EU5) {
        @Override
        protected int determineHeaderVersion(SavegameContent content) {
            var gamestate = content.get("gamestate");
            var version = gamestate.getNodeForKeys("metadata", "version");
            var p = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)");
            var matcher = p.matcher(version.getString());
            if (matcher.matches()) {
                var requiresV2 = Integer.parseInt(matcher.group(1)) > 1 ||
                        Integer.parseInt(matcher.group(2)) > 0 ||
                        Integer.parseInt(matcher.group(3)) >= 8;
                return requiresV2 ? 2 : 1;
            } else {
                return 1;
            }
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

    void write(Path out, SavegameContent content) throws IOException;

    default void writeData(OutputStream out, ArrayNode node) throws IOException {
        NodeWriter.write(out, getType().getParser().getCharset(), node, "\t", 0);
    }

    SavegameParseResult parse(byte[] input);

    SavegameType getType();
}

package com.crschnick.pdxu.io.savegame;

import com.crschnick.pdxu.io.node.ArrayNode;
import com.crschnick.pdxu.io.node.NodeWriter;
import com.crschnick.pdxu.io.parser.TextFormatParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Ck3CompressedSavegameStructure extends ZipSavegameStructure {

    public Ck3CompressedSavegameStructure() {
        super(null, TextFormatParser.CK3, Set.of(new SavegamePart("gamestate", "gamestate")));
    }

    private static final int MAX_SEARCH = 150000;
    private static final byte[] ZIP_HEADER = new byte[] {0x50, 0x4B, 0x03, 0x04};

    private static int indexOf(byte[] array, byte[] toFind) {
        for (int i = 0; i < MAX_SEARCH; ++i) {
            boolean found = true;
            for (int j = 0; j < toFind.length; ++j) {
                if (array[i + j] != toFind[j]) {
                    found = false;
                    break;
                }
            }
            if (found) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void write(Path file, Map<String, ArrayNode> nodes) throws IOException {
        var gamestate = nodes.get("gamestate");
        ArrayNode meta = (ArrayNode) gamestate.getNodeForKey("meta_data");
        var metaHeaderNode = ArrayNode.singleKeyNode("meta_data", meta);

        try (var out = Files.newOutputStream(file)) {
            var metaBytes = NodeWriter.writeToBytes(metaHeaderNode, Integer.MAX_VALUE, "\t");

            // Exclude trailing new line in meta length!
            String header = new Ck3Header(true, true, false, metaBytes.length).toString();
            out.write((header + "\n").getBytes(StandardCharsets.UTF_8));
            out.write(metaBytes);
            try (var zout = new ZipOutputStream(out)) {
                zout.putNextEntry(new ZipEntry("gamestate"));
                NodeWriter.write(zout, StandardCharsets.UTF_8, gamestate, "\t", 0);
                zout.closeEntry();
            }
        }
    }

    @Override
    public SavegameParseResult parse(byte[] input) {
        var header = Ck3Header.fromStartOfFile(input);
        if (header.binary()) {
            throw new IllegalArgumentException("Binary savegames are not supported");
        }
        if (!header.compressed()) {
            throw new IllegalArgumentException("Uncompressed savegames are not supported");
        }

        int metaStart = header.toString().length() + 1;
        int contentStart = (int) (metaStart + header.metaLength());

        // Check if the header meta length is actually right. If not, manually search for the zip header start
        if (!Arrays.equals(input, contentStart, contentStart + 4, ZIP_HEADER, 0, 4)) {
            contentStart = indexOf(input, ZIP_HEADER);
        }

        return parseInput(input, contentStart);
    }
}

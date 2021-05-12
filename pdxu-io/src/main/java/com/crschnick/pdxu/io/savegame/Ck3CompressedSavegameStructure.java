package com.crschnick.pdxu.io.savegame;

import com.crschnick.pdxu.io.node.ArrayNode;
import com.crschnick.pdxu.io.node.LinkedArrayNode;
import com.crschnick.pdxu.io.node.NodeWriter;
import com.crschnick.pdxu.io.node.TaggedNode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Ck3CompressedSavegameStructure extends ZipSavegameStructure {

    public Ck3CompressedSavegameStructure() {
        super(null, StandardCharsets.UTF_8, TaggedNode.COLORS, Set.of(new SavegamePart("gamestate", "gamestate")));
    }

    public static void writeCompressed(byte[] input, Path output) throws IOException {
        var inputHeader = Ck3Header.fromStartOfFile(input);
        if (inputHeader.compressed()) {
            throw new IllegalArgumentException("Savegame is already compressed");
        }

        var header = new Ck3Header(true, inputHeader.binary(),
                inputHeader.randomness(), inputHeader.metaLength());
        int metaStart = header.toString().length() + 1;
        try (var out = Files.newOutputStream(output)) {
            out.write((header + "\n").getBytes(StandardCharsets.UTF_8));
            out.write(input, metaStart, (int) header.metaLength());
            if (!header.binary()) {
                out.write("\n".getBytes(StandardCharsets.UTF_8));
            }
            try (var zout = new ZipOutputStream(out)) {
                zout.putNextEntry(new ZipEntry("gamestate"));
                zout.write(input, metaStart, input.length - metaStart);
                zout.closeEntry();
            }
        }
    }

    public static void writeCompressedWithDisabledIronman(byte[] input, Path output) throws IOException {
        var header = Ck3Header.fromStartOfFile(input);
        if (header.compressed()) {
            throw new IllegalArgumentException("Savegame is already compressed");
        }
        if (header.binary()) {
            throw new IllegalArgumentException("Savegame is not in plaintext format");
        }

        int metaStart = header.toString().length() + 1;
        String metaData = new String(input, metaStart, (int) header.metaLength());
        metaData = metaData.replace("can_get_achievements=yes", "can_get_achievements=no");
        metaData = metaData.replace("ironman=yes", "ironman=no");
        var newMetaBytes = metaData.getBytes(StandardCharsets.UTF_8);
        var newHeader = new Ck3Header(true, false, newMetaBytes.length);

        int contentStart = (int) (metaStart + header.metaLength() + 1);
        String ironmanManager = new String(input, contentStart, 1000);
        ironmanManager = ironmanManager.replace("ironman=yes", "ironman=no");

        try (var out = Files.newOutputStream(output)) {
            out.write((newHeader + "\n").getBytes(StandardCharsets.UTF_8));
            out.write(input, metaStart, (int) newHeader.metaLength());
            out.write("\n".getBytes(StandardCharsets.UTF_8));
            try (var zout = new ZipOutputStream(out)) {
                zout.putNextEntry(new ZipEntry("gamestate"));
                zout.write(ironmanManager.getBytes(StandardCharsets.UTF_8));
                zout.write(input, contentStart, input.length - (contentStart + 1000));
                zout.closeEntry();
            }
        }
    }

    @Override
    public void write(Path file, Map<String, ArrayNode> nodes) throws IOException {
        var gamestate = nodes.get("gamestate");
        ArrayNode meta = (ArrayNode) gamestate.getNodeForKey("meta_data");
        var metaHeaderNode = ArrayNode.singleKeyNode("meta_data", meta);

        try (var out = Files.newOutputStream(file)) {
            var metaBytes = NodeWriter.writeToBytes(metaHeaderNode, Integer.MAX_VALUE, "\t");

            // Exclude trailing new line in meta length!
            String header = new Ck3Header(true, false, metaBytes.length - 1).toString();
            out.write((header + "\n").getBytes(StandardCharsets.UTF_8));
            out.write(metaBytes);
            try (var zout = new ZipOutputStream(out)) {
                zout.putNextEntry(new ZipEntry("gamestate"));
                NodeWriter.write(zout, StandardCharsets.UTF_8, new LinkedArrayNode(List.of(metaHeaderNode, gamestate)), "\t");
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
        int contentStart = (int) (metaStart + header.metaLength()) + 1;
        return parseInput(input, contentStart);
    }
}

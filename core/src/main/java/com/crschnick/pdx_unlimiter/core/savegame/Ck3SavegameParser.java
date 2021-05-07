package com.crschnick.pdx_unlimiter.core.savegame;

import com.crschnick.pdx_unlimiter.core.info.ck3.Ck3Header;
import com.crschnick.pdx_unlimiter.core.info.ck3.Ck3SavegameInfo;
import com.crschnick.pdx_unlimiter.core.node.ArrayNode;
import com.crschnick.pdx_unlimiter.core.node.LinkedArrayNode;
import com.crschnick.pdx_unlimiter.core.node.Node;
import com.crschnick.pdx_unlimiter.core.parser.NodeWriter;
import com.crschnick.pdx_unlimiter.core.parser.TextFormatParser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public final class Ck3SavegameParser extends SavegameParser {

    public static boolean isCompressed(Path file) throws IOException {
        try (var stream = Files.lines(file)) {
            var first = stream.findFirst();
            if (first.isEmpty()) {
                return false;
            }

            var header = Ck3Header.fromString(first.get());
            return header.compressed();
        }
    }

    public void writeCompressedIfPossible(byte[] input, Path output) throws IOException {
        writeCompressedIfPossible(input, output, false);
    }

    public void writeCompressedIfPossible(byte[] input, Path output, boolean disableIronman) throws IOException {
        var header = Ck3Header.fromStartOfFile(input);
        if (header.compressed()) {
            Files.write(output, input);
            return;
        }

        if (!header.binary() && disableIronman) {
            writeCompressedWithDisabledIronman(input, output);
        } else {
            writeCompressed(input, output);
        }
    }

    private static void writeCompressed(byte[] input, Path output) throws IOException {
        var inputHeader = Ck3Header.fromStartOfFile(input);
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

    private static void writeCompressedWithDisabledIronman(byte[] input, Path output) throws IOException {
        var header = Ck3Header.fromStartOfFile(input);
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

    public static void writeUncompressedPlaintext(Path output, ArrayNode gamestate) throws IOException {
        ArrayNode meta = (ArrayNode) gamestate.getNodeForKey("meta_data");
        var metaHeaderNode = ArrayNode.singleKeyNode("meta_data", meta);

        try (var out = Files.newOutputStream(output)) {
            var metaBytes = NodeWriter.writeToBytes(metaHeaderNode, Integer.MAX_VALUE, "\t");
            // Exclude trailing new line in meta length!
            String header = new Ck3Header(false, false, metaBytes.length).toString();
            out.write((header + "\n").getBytes(StandardCharsets.UTF_8));

            NodeWriter.write(out, StandardCharsets.UTF_8, gamestate, "\t");
        }
    }

    public static void writeCompressedPlaintext(Path output, ArrayNode gamestate) throws IOException {
        ArrayNode meta = (ArrayNode) gamestate.getNodeForKey("meta_data");
        var metaHeaderNode = ArrayNode.singleKeyNode("meta_data", meta);

        try (var out = Files.newOutputStream(output)) {
            var metaBytes = NodeWriter.writeToBytes(metaHeaderNode, Integer.MAX_VALUE, "\t");

            // Exclude trailing new line in meta length!
            String header = new Ck3Header(true, false, metaBytes.length).toString();
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
    public Status parse(Path input, Melter melter) {
        Node gamestate = null;
        try {
            var content = Files.readAllBytes(input);
            String checksum = checksum(content);

            var header = Ck3Header.fromStartOfFile(content);

            boolean binary = header.binary();
            boolean melted = false;
            var fileToParse = input;
            if (binary) {
                fileToParse = melter.melt(input);
                melted = true;
            }

            content = Files.readAllBytes(fileToParse);
            int metaStart = header.toString().length() + 1;
            header = Ck3Header.fromStartOfFile(content);

            boolean compressed = header.compressed();
            if (compressed) {
                int contentStart = (int) (metaStart + header.metaLength()) + (header.binary() ? 0 : 1);
                try (var zipIn = new ZipInputStream(new ByteArrayInputStream(content,
                        contentStart, content.length - contentStart))) {
                    var entry = zipIn.getNextEntry();
                    if (entry == null) {
                        return new Invalid("Gamestate not found");
                    }

                    var gamestateData = zipIn.readAllBytes();
                    gamestate = TextFormatParser.ck3SavegameParser().parse(gamestateData);
                }
            } else {
                gamestate = TextFormatParser.ck3SavegameParser().parse(content, header.toString().length() + 1);
            }

            return new Success<>(checksum, gamestate, Ck3SavegameInfo.fromSavegame(melted, gamestate), content);
        } catch (Throwable e) {
            return new Error(e, gamestate);
        }
    }
}

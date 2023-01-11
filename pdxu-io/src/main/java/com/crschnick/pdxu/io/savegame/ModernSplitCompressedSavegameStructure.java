package com.crschnick.pdxu.io.savegame;

import com.crschnick.pdxu.io.node.NodeWriter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ModernSplitCompressedSavegameStructure extends ZipSavegameStructure {

    public ModernSplitCompressedSavegameStructure(SavegameType type) {
        super(null, type, Set.of(new SavegamePart("gamestate", "gamestate"), new SavegamePart("meta", "meta")));
    }

    private static final int MAX_SEARCH = 150000;
    public static final byte[] ZIP_HEADER = new byte[] {0x50, 0x4B, 0x03, 0x04};

    public static int indexOfCompressedGamestateStart(byte[] array) {
        if (array.length < ZIP_HEADER.length) {
            return -1;
        }

        int end = Math.min(array.length, MAX_SEARCH - ZIP_HEADER.length);
        for (int i = 0; i < end; ++i) {
            boolean found = true;
            for (int j = 0; j < ZIP_HEADER.length; ++j) {
                if (array[i + j] != ZIP_HEADER[j]) {
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
    public void write(Path file, SavegameContent content) throws IOException {
        var gamestate = content.get("gamestate");
        var meta = content.get("meta");

        try (var out = Files.newOutputStream(file)) {
            // Exclude trailing new line in meta length!
            String header = new ModernHeader(true, 2, false, 0).toString();
            out.write((header + "\n").getBytes(StandardCharsets.UTF_8));
            try (var zout = new ZipOutputStream(out)) {
                zout.putNextEntry(new ZipEntry("gamestate"));
                NodeWriter.write(zout, StandardCharsets.UTF_8, gamestate, "\t", 0);
                zout.closeEntry();

                zout.putNextEntry(new ZipEntry("meta"));
                NodeWriter.write(zout, StandardCharsets.UTF_8, meta, "\t", 0);
                zout.closeEntry();
            }
        }
    }

    @Override
    public SavegameParseResult parse(byte[] input) {
        int contentStart;
        if (ModernHeader.skipsHeader(input)) {
            contentStart = 0;
        } else {
            var header = ModernHeader.determineHeaderForFile(input);
            if (header.binary()) {
                throw new IllegalArgumentException("Binary savegames are not supported");
            }
            if (!header.isSplitCompressed()) {
                throw new IllegalArgumentException("Uncompressed savegames are not supported");
            }

            contentStart = header.toString().length() + 1;
        }

        // Check if the header meta length is actually right. If not, manually search for the zip header start
        if (!Arrays.equals(input, contentStart, contentStart + 4, ZIP_HEADER, 0, 4)) {
            contentStart = indexOfCompressedGamestateStart(input);
            if (contentStart == -1) {
                throw new IllegalArgumentException("Zip start not found in savegame");
            }
        }

        return parseInput(input, contentStart);
    }
}

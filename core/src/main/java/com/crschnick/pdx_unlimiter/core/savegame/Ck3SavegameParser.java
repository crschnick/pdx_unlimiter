package com.crschnick.pdx_unlimiter.core.savegame;

import com.crschnick.pdx_unlimiter.core.parser.Node;
import com.crschnick.pdx_unlimiter.core.parser.TextFormatParser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.zip.ZipInputStream;

public class Ck3SavegameParser extends SavegameParser<Ck3SavegameInfo> {

    private static final int MAX_SEARCH = 100000;

    private int indexOf(byte[] array, byte[] toFind, int maxSearch) {
        for (int i = 0; i < maxSearch; ++i) {
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

    public boolean isCompressed(Path file) throws IOException {
        var content = Files.readAllBytes(file);
        int zipContentStart = indexOf(content, "}\nPK".getBytes(), MAX_SEARCH);
        boolean compressed = zipContentStart != 1;
        return false;
    }

    @Override
    public Status parse(Path input, Melter melter) {
        try {
            var content = Files.readAllBytes(input);
            String checksum = checksum(content);

            var contentString = new String(content, StandardCharsets.UTF_8);
            var first = contentString.lines().findFirst();
            if (first.isEmpty()) {
                return new Invalid("Empty savegame content");
            }
            int metaStart = first.get().length() + 1;
            boolean binary = !contentString.startsWith("meta", metaStart);

            boolean melted = false;
            var fileToParse = input;
            if (binary) {
                fileToParse = melter.melt(input);
                melted = true;
            }

            content = Files.readAllBytes(fileToParse);
            byte[] savegameText;
            int zipContentStart = indexOf(content, "}\nPK".getBytes(), MAX_SEARCH) + 2;
            boolean compressed = zipContentStart != 1;
            if (compressed) {
                byte[] zipContent = Arrays.copyOfRange(content, zipContentStart, content.length);
                var zipIn = new ZipInputStream(new ByteArrayInputStream(zipContent));
                zipIn.getNextEntry();
                savegameText = zipIn.readAllBytes();
                zipIn.close();
            } else {
                savegameText = content;
            }

            Node node = TextFormatParser.ck3SavegameParser().parse(savegameText);
            return new Success<>(binary, checksum, node, Ck3SavegameInfo.fromSavegame(melted, node));
        } catch (Exception e) {
            return new Error(e);
        }
    }
}

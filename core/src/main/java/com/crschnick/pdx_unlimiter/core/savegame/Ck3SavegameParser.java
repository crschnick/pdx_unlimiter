package com.crschnick.pdx_unlimiter.core.savegame;

import com.crschnick.pdx_unlimiter.core.info.ck3.Ck3SavegameInfo;
import com.crschnick.pdx_unlimiter.core.node.Node;
import com.crschnick.pdx_unlimiter.core.parser.TextFormatParser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipInputStream;

public class Ck3SavegameParser extends SavegameParser {

    private static final int MAX_SEARCH = 150000;

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
    private static final byte[] ZIP_HEADER = new byte[] {0x50, 0x4B, 0x03, 0x04};

    public static boolean isCompressed(Path file) throws IOException {
        var content = Files.readAllBytes(file);
        int zipContentStart = indexOf(content, ZIP_HEADER);
        return zipContentStart != -1;
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
            int zipContentStart = indexOf(content, ZIP_HEADER);
            boolean compressed = zipContentStart != -1;
            if (compressed) {
                var zipIn = new ZipInputStream(new ByteArrayInputStream(content,
                        zipContentStart, content.length - zipContentStart));
                zipIn.getNextEntry();
                savegameText = zipIn.readAllBytes();
                zipIn.close();
            } else {
                savegameText = content;
            }

            Node node = TextFormatParser.ck3SavegameParser().parse(savegameText);
            return new Success<>(binary, checksum, node, Ck3SavegameInfo.fromSavegame(melted, node));
        } catch (Throwable e) {
            return new Error(e);
        }
    }
}

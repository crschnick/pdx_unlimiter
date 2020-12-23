package com.crschnick.pdx_unlimiter.core.savegame;

import com.crschnick.pdx_unlimiter.core.parser.Node;
import com.crschnick.pdx_unlimiter.core.parser.TextFormatParser;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.ZipInputStream;

public class Ck3SavegameParser extends SavegameParser {

    public boolean isBinaryFormat(byte[] content) {
        var contentString = new String(content, StandardCharsets.UTF_8);
        String first = contentString.lines().findFirst().get();
        int metaStart = first.length() + 1;
        boolean binary = !contentString.startsWith("meta", metaStart);
        return binary;
    }

    private static final int MAX_SEARCH = 100000;

    public Node parse(byte[] content) throws Exception {
        var contentString = new String(content, StandardCharsets.UTF_8);
        String first = contentString.lines().findFirst()
                .orElseThrow(() -> new SavegameParseException("Empty savegame content"));

        byte[] data;
        int zipContentStart = indexOf(content, "}\nPK".getBytes(), MAX_SEARCH) + 2;
        boolean compressed = zipContentStart != 1;
        if (compressed) {
            byte[] zipContent = Arrays.copyOfRange(content, zipContentStart, content.length);
            var zipIn = new ZipInputStream(new ByteArrayInputStream(zipContent));
            zipIn.getNextEntry();
            data = zipIn.readAllBytes();
            zipIn.close();
        } else {
            data = content;
        }

        var parser = TextFormatParser.textFileParser();
        return parser.parse(data);
    }

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
}

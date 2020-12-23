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

    public Node parse(byte[] content) throws Exception {
        var contentString = new String(content, StandardCharsets.UTF_8);
        String first = contentString.lines().findFirst().get();
        int metaStart = first.length() + 1;
        boolean binary = !contentString.startsWith("meta", metaStart);
        int metaEnd = binary ? indexOf(content, "PK".getBytes()) : (indexOf(content, "}\nPK".getBytes()) + 2);
        byte[] zipContent = Arrays.copyOfRange(content, metaEnd, content.length);
        var zipIn = new ZipInputStream(new ByteArrayInputStream(zipContent));
        var gamestateEntry = zipIn.getNextEntry();

        boolean isZipped = gamestateEntry != null;
        if (!isZipped) {
            throw new SavegameParseException("Ck3 gamestate must be zipped");
        }

        var parser = TextFormatParser.textFileParser();
        Node node = parser.parse(zipIn);
        zipIn.close();

        var duplicateMeta = node.getNodeForKey("meta_data");
        node.getNodeArray().remove(duplicateMeta);

        return node;
    }

    private int indexOf(byte[] array, byte[] toFind) {
        for (int i = 0; i < array.length - toFind.length + 1; ++i) {
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
        throw new IllegalArgumentException("Array not found");
    }
}

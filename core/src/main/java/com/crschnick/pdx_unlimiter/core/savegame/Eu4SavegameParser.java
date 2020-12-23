package com.crschnick.pdx_unlimiter.core.savegame;

import com.crschnick.pdx_unlimiter.core.parser.FormatParser;
import com.crschnick.pdx_unlimiter.core.parser.Node;
import com.crschnick.pdx_unlimiter.core.parser.TextFormatParser;

import java.io.ByteArrayInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Eu4SavegameParser extends SavegameParser {

    private static final byte[] EU4_BINARY_HEADER = new byte[]{0x45, 0x55, 0x34, 0x62, 0x69, 0x6E};

    @Override
    public boolean isBinaryFormat(byte[] content) throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(content);
        boolean isZipped = new ZipInputStream(in).getNextEntry() != null;
        in.close();

        if (!isZipped) {
            return false;
        }

        in = new ByteArrayInputStream(content);
        var zipFile = new ZipInputStream(in);
        ZipEntry entry;
        while ((entry = zipFile.getNextEntry()) != null) {
            if (entry.getName().equals("gamestate")) {
                return FormatParser.validateHeader(EU4_BINARY_HEADER, zipFile);
            }
            zipFile.closeEntry();
        }
        zipFile.close();
        return false;
    }

    @Override
    public Node parse(byte[] content) throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(content);
        boolean isZipped = new ZipInputStream(in).getNextEntry() != null;
        in.close();

        if (isZipped) {
            in = new ByteArrayInputStream(content);
            var zipFile = new ZipInputStream(in);
            Node gamestate = null;
            Node meta = null;
            Node ai = null;

            ZipEntry entry;
            while ((entry = zipFile.getNextEntry()) != null) {
                if (entry.getName().equals("gamestate")) {
                    gamestate = TextFormatParser.eu4SavegameParser().parse(zipFile);
                }
                if (entry.getName().equals("meta")) {
                    meta = TextFormatParser.eu4SavegameParser().parse(zipFile);
                }
                if (entry.getName().equals("ai")) {
                    ai = TextFormatParser.eu4SavegameParser().parse(zipFile);
                }
            }
            zipFile.close();
            in.close();

            return Node.combine(gamestate, meta, ai);
        } else {
            return TextFormatParser.eu4SavegameParser().parse(new ByteArrayInputStream(content));
        }
    }
}

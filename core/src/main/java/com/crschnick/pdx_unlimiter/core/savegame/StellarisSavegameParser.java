package com.crschnick.pdx_unlimiter.core.savegame;

import com.crschnick.pdx_unlimiter.core.parser.Node;
import com.crschnick.pdx_unlimiter.core.parser.TextFormatParser;

import java.io.ByteArrayInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class StellarisSavegameParser extends SavegameParser {

    @Override
    public boolean isBinaryFormat(byte[] content) throws Exception {
        return false;
    }

    @Override
    public Node parse(byte[] content) throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(content);
        var zipFile = new ZipInputStream(in);
        Node gamestate = null;
        Node meta = null;

        ZipEntry entry;
        while ((entry = zipFile.getNextEntry()) != null) {
            if (entry.getName().equals("gamestate")) {
                gamestate = TextFormatParser.eu4SavegameParser().parse(zipFile);
            }
            if (entry.getName().equals("meta")) {
                meta = TextFormatParser.eu4SavegameParser().parse(zipFile);
            }
            zipFile.closeEntry();
        }
        zipFile.close();
        return Node.combine(gamestate, meta);
    }
}

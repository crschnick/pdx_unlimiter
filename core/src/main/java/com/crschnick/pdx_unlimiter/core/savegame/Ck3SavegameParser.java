package com.crschnick.pdx_unlimiter.core.savegame;

import com.crschnick.pdx_unlimiter.core.info.ck3.Ck3Header;
import com.crschnick.pdx_unlimiter.core.info.ck3.Ck3SavegameInfo;
import com.crschnick.pdx_unlimiter.core.node.Node;
import com.crschnick.pdx_unlimiter.core.parser.TextFormatParser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipInputStream;

public class Ck3SavegameParser extends SavegameParser {

    public static boolean isCompressed(Path file) throws IOException {
        var content = Files.readAllBytes(file);
        var header = Ck3Header.fromStartOfFile(content);
        return header.compressed();
    }

    @Override
    public Status parse(Path input, Melter melter) {
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
            Node gamestate;
            if (compressed) {
                int contentStart = (int) (metaStart + header.metaLength() + (binary ? 0 : 1));
                try (var zipIn = new ZipInputStream(new ByteArrayInputStream(content,
                        contentStart, content.length - contentStart))) {
                    zipIn.getNextEntry();
                    var gamestateData = zipIn.readAllBytes();
                    gamestate = TextFormatParser.ck3SavegameParser().parse(gamestateData);
                }
            } else {
                gamestate = TextFormatParser.ck3SavegameParser().parse(content, header.toString().length() + 1);
            }

            return new Success<>(binary, checksum, gamestate, Ck3SavegameInfo.fromSavegame(melted, gamestate));
        } catch (Throwable e) {
            return new Error(e);
        }
    }
}

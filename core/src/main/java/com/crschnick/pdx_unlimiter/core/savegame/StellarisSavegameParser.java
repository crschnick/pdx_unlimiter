package com.crschnick.pdx_unlimiter.core.savegame;

import com.crschnick.pdx_unlimiter.core.parser.Node;
import com.crschnick.pdx_unlimiter.core.parser.TextFormatParser;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipFile;

public class StellarisSavegameParser extends SavegameParser<StellarisSavegameInfo> {

    @Override
    public Status parse(Path input, Melter melter) {
        try {
            String checksum = checksum(Files.readAllBytes(input));

            var zipFile = new ZipFile(input.toFile());
            Node gamestateNode = null;
            Node metaNode = null;

            var gs = zipFile.getEntry("gamestate");
            if (gs == null) {
                return new Invalid("Missing gamestate");
            }
            var gsIn = zipFile.getInputStream(gs);
            gamestateNode = TextFormatParser.textFileParser().parse(gsIn);

            var mt = zipFile.getEntry("meta");
            if (mt == null) {
                return new Invalid("Missing meta");
            }
            var mtIn = zipFile.getInputStream(mt);
            metaNode = TextFormatParser.textFileParser().parse(mtIn);

            zipFile.close();

            var node = Node.combine(gamestateNode, metaNode);
            return new Success<>(true, checksum, node, StellarisSavegameInfo.fromSavegame(node));
        } catch (Exception e) {
            return new Error(e);
        }
    }
}

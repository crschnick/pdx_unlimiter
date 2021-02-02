package com.crschnick.pdx_unlimiter.core.info.stellaris;

import com.crschnick.pdx_unlimiter.core.parser.Node;
import com.crschnick.pdx_unlimiter.core.parser.TextFormatParser;
import com.crschnick.pdx_unlimiter.core.savegame.SavegameParser;
import com.crschnick.pdx_unlimiter.core.savegame.StellarisSavegameInfo;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipFile;

public class StellarisSavegameParser extends SavegameParser {

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
            gamestateNode = TextFormatParser.stellarisSavegameParser().parse(gsIn.readAllBytes());

            var mt = zipFile.getEntry("meta");
            if (mt == null) {
                return new Invalid("Missing meta");
            }
            var mtIn = zipFile.getInputStream(mt);
            metaNode = TextFormatParser.stellarisSavegameParser().parse(mtIn.readAllBytes());

            zipFile.close();

            var node = Node.combine(gamestateNode, metaNode);
            return new Success<>(true, checksum, node, StellarisSavegameInfo.fromSavegame(node));
        } catch (Exception e) {
            return new Error(e);
        }
    }
}

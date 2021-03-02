package com.crschnick.pdx_unlimiter.core.savegame;

import com.crschnick.pdx_unlimiter.core.info.stellaris.StellarisSavegameInfo;
import com.crschnick.pdx_unlimiter.core.node.ArrayNode;
import com.crschnick.pdx_unlimiter.core.node.LinkedNode;
import com.crschnick.pdx_unlimiter.core.node.Node;
import com.crschnick.pdx_unlimiter.core.parser.TextFormatParser;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipFile;

public class StellarisSavegameParser extends SavegameParser {

    @Override
    public Status parse(Path input, Melter melter) {
        try {
            String checksum = checksum(Files.readAllBytes(input));

            var zipFile = new ZipFile(input.toFile());
            ArrayNode gamestateNode = null;
            ArrayNode metaNode = null;

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

            var node = new LinkedNode(List.of(metaNode, gamestateNode));
            return new Success<>(true, checksum, node, StellarisSavegameInfo.fromSavegame(node));
        } catch (Throwable e) {
            return new Error(e);
        }
    }
}

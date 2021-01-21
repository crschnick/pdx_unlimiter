package com.crschnick.pdx_unlimiter.core.savegame;

import com.crschnick.pdx_unlimiter.core.data.GameVersion;
import com.crschnick.pdx_unlimiter.core.parser.FormatParser;
import com.crschnick.pdx_unlimiter.core.parser.Node;
import com.crschnick.pdx_unlimiter.core.parser.TextFormatParser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class Eu4SavegameParser extends SavegameParser<Eu4SavegameInfo> {

    private static final GameVersion MIN_VERSION = new GameVersion(1, 28, 0, 0, null);

    private static final byte[] EU4_TEXT_HEADER = "EU4txt".getBytes(StandardCharsets.UTF_8);
    private static final byte[] EU4_BINARY_HEADER = new byte[]{0x45, 0x55, 0x34, 0x62, 0x69, 0x6E};

    public boolean isCompressed(Path file) throws IOException {
        boolean isZipped = false;
        try (var in = Files.newInputStream(file);
             var zipIn = new ZipInputStream(in)){
            isZipped = zipIn.getNextEntry() != null;
        }
        return isZipped;
    }

    public boolean isBinary(Path input) throws IOException {
        try (var fs = FileSystems.newFileSystem(input);
            var in = Files.newInputStream(fs.getPath("gamestate"))) {
            return FormatParser.validateHeader(EU4_BINARY_HEADER, in);
        }
    }

    @Override
    public Status parse(Path input, Melter melter) {
        try {
            String checksum = checksum(Files.readAllBytes(input));

            boolean isZipped = isCompressed(input);

            boolean melted = false;
            var fileToParse = input;
            if (isZipped) {
                if (isBinary(input)) {
                    fileToParse = melter.melt(input);
                    melted = true;
                    isZipped = false;
                }
            }

            if (!isZipped) {
                try (var in = Files.newInputStream(fileToParse)){
                    boolean valid = FormatParser.validateHeader(EU4_TEXT_HEADER, in);
                    if (!valid) {
                        return new Invalid("Invalid header");
                    }

                    var content = in.readAllBytes();
                    var node = TextFormatParser.eu4SavegameParser().parse(content);
                    var info = Eu4SavegameInfo.fromSavegame(melted, node);
                    if (info.version.compareTo(MIN_VERSION) < 0) {
                        return new Invalid("Savegame version " + info.version + " is not supported");
                    }
                    return new Success<>(false, checksum, node, info);
                }
            } else {
                try (var fs = FileSystems.newFileSystem(fileToParse)) {
                    Node gamestateNode = null;
                    Node metaNode = null;
                    Node aiNode = null;

                    var gs = fs.getPath("gamestate");
                    if (!Files.exists(gs)) {
                        return new Invalid("Missing gamestate. This might be a very old savegame, which is not supported");
                    }
                    var gsIn = Files.newInputStream(gs);
                    if (FormatParser.validateHeader(EU4_TEXT_HEADER, gsIn)) {
                        gamestateNode = TextFormatParser.eu4SavegameParser().parse(gsIn.readAllBytes());
                    } else {
                        return new Invalid("Invalid header for gamestate");
                    }

                    var mt = fs.getPath("meta");
                    if (!Files.exists(mt)) {
                        return new Invalid("Missing meta");
                    }
                    var mtIn = Files.newInputStream(mt);
                    if (FormatParser.validateHeader(EU4_TEXT_HEADER, mtIn)) {
                        metaNode = TextFormatParser.eu4SavegameParser().parse(mtIn.readAllBytes());
                    } else {
                        return new Invalid("Invalid header for meta");
                    }

                    var ai = fs.getPath("ai");
                    if (!Files.exists(ai)) {
                        return new Invalid("Missing ai");
                    }
                    var aiIn = Files.newInputStream(ai);
                    if (FormatParser.validateHeader(EU4_TEXT_HEADER, aiIn)) {
                        aiNode = TextFormatParser.eu4SavegameParser().parse(aiIn.readAllBytes());
                    } else {
                        return new Invalid("Invalid header for ai");
                    }

                    var node = Node.combine(gamestateNode, metaNode, aiNode);
                    var info = Eu4SavegameInfo.fromSavegame(melted, node);
                    if (info.version.compareTo(MIN_VERSION) < 0) {
                        return new Invalid("Savegame version " + info.version + " is not supported");
                    }
                    return new Success<>(true, checksum, node, info);
                }
            }
        } catch (Exception e) {
            return new Error(e);
        }
    }
}

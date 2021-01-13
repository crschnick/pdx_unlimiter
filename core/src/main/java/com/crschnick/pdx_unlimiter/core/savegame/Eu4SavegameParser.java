package com.crschnick.pdx_unlimiter.core.savegame;

import com.crschnick.pdx_unlimiter.core.parser.FormatParser;
import com.crschnick.pdx_unlimiter.core.parser.Node;
import com.crschnick.pdx_unlimiter.core.parser.TextFormatParser;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipUtil;
import org.apache.commons.compress.utils.ArchiveUtils;
import org.w3c.dom.Text;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class Eu4SavegameParser extends SavegameParser<Eu4SavegameInfo> {

    private static final byte[] EU4_TEXT_HEADER = "EU4txt".getBytes(StandardCharsets.UTF_8);
    private static final byte[] EU4_BINARY_HEADER = new byte[]{0x45, 0x55, 0x34, 0x62, 0x69, 0x6E};

    @Override
    public Status parse(Path input, Melter melter) {
        try {
            String checksum = checksum(Files.readAllBytes(input));

            var in = Files.newInputStream(input);
            var zipIn = new ZipInputStream(in);
            boolean isZipped = zipIn.getNextEntry() != null;
            zipIn.close();
            in.close();

            boolean melted = false;
            var fileToParse = input;
            if (isZipped) {
                var zipFile = new ZipFile(input.toFile());
                var gs = zipFile.getEntry("gamestate");
                if (gs != null) {
                    var eIn = zipFile.getInputStream(gs);
                    if (FormatParser.validateHeader(EU4_BINARY_HEADER, eIn)) {
                        fileToParse = melter.melt(input);
                        melted = true;
                        isZipped = false;
                    }
                }
            }

            if (!isZipped) {
                in = Files.newInputStream(fileToParse);
                boolean valid = FormatParser.validateHeader(EU4_TEXT_HEADER, in);
                if (!valid) {
                    in.close();
                    return new Invalid("Invalid header");
                }

                var content = in.readAllBytes();
                var node = TextFormatParser.eu4SavegameParser().parse(content);
                return new Success<>(false, checksum, node, Eu4SavegameInfo.fromSavegame(melted, node));
            } else {
                var zipFile = new ZipFile(fileToParse.toFile());
                Node gamestateNode = null;
                Node metaNode = null;
                Node aiNode = null;

                var gs = zipFile.getEntry("gamestate");
                if (gs == null) {
                    return new Invalid("Missing gamestate");
                }
                var gsIn = zipFile.getInputStream(gs);
                if (FormatParser.validateHeader(EU4_TEXT_HEADER, gsIn)) {
                    gamestateNode = TextFormatParser.textFileParser().parse(gsIn);
                } else {
                    return new Invalid("Invalid header for gamestate");
                }

                var mt = zipFile.getEntry("meta");
                if (mt == null) {
                    return new Invalid("Missing meta");
                }
                var mtIn = zipFile.getInputStream(mt);
                if (FormatParser.validateHeader(EU4_TEXT_HEADER, mtIn)) {
                    metaNode = TextFormatParser.textFileParser().parse(mtIn);
                } else {
                    return new Invalid("Invalid header for meta");
                }

                var ai = zipFile.getEntry("ai");
                if (ai == null) {
                    return new Invalid("Missing ai");
                }
                var aiIn = zipFile.getInputStream(ai);
                if (FormatParser.validateHeader(EU4_TEXT_HEADER, aiIn)) {
                    aiNode = TextFormatParser.textFileParser().parse(aiIn);
                } else {
                    return new Invalid("Invalid header for ai");
                }

                zipFile.close();

                var node = Node.combine(gamestateNode, metaNode, aiNode);
                return new Success<>(true, checksum, node, Eu4SavegameInfo.fromSavegame(melted, node));
            }
        } catch (Exception e) {
            return new Error(e);
        }
    }
}

package com.crschnick.pdx_unlimiter.core.savegame;

import com.crschnick.pdx_unlimiter.core.info.GameVersion;
import com.crschnick.pdx_unlimiter.core.info.SavegameInfo;
import com.crschnick.pdx_unlimiter.core.info.eu4.Eu4SavegameInfo;
import com.crschnick.pdx_unlimiter.core.node.ArrayNode;
import com.crschnick.pdx_unlimiter.core.node.LinkedArrayNode;
import com.crschnick.pdx_unlimiter.core.parser.FormatParser;
import com.crschnick.pdx_unlimiter.core.parser.TextFormatParser;
import com.crschnick.pdx_unlimiter.sentry_utils.SentryHelper;
import io.sentry.ISpan;
import io.sentry.SpanStatus;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipInputStream;

public class Eu4SavegameParser extends SavegameParser {

    private static final GameVersion MIN_VERSION = new GameVersion(1, 28, 0, 0, null);

    private static final byte[] EU4_TEXT_HEADER = "EU4txt".getBytes(StandardCharsets.UTF_8);
    private static final byte[] EU4_BINARY_HEADER = "EU4bin".getBytes(StandardCharsets.UTF_8);

    public boolean isCompressed(Path file) throws IOException {
        boolean isZipped;
        try (var in = Files.newInputStream(file);
             var zipIn = new ZipInputStream(in)) {
            isZipped = zipIn.getNextEntry() != null;
        }
        return isZipped;
    }

    public boolean isBinary(Path input) throws IOException {
        try (var fs = FileSystems.newFileSystem(input)) {
            var gs = fs.getPath("gamestate");

            // If it is very old and does not contain a gamestate, just return false to avoid melting
            if (!Files.exists(gs)) {
                return false;
            }
            try (var in = Files.newInputStream(gs)) {
                return FormatParser.validateHeader(EU4_BINARY_HEADER, in);
            }
        }
    }

    @Override
    public Status parse(Path input, Melter melter) {
        var span = SentryHelper.getSpan();
        ISpan op = null;

        try {
            op = span.startChild("checksum");
            String checksum = checksum(Files.readAllBytes(input));
            op.finish();

            op = span.startChild("checkCompressed");
            boolean isZipped = isCompressed(input);
            op.setTag("compressed", String.valueOf(isZipped));
            op.finish();

            op = span.startChild("checkMelt");
            boolean shouldMelt = isZipped && isBinary(input);
            op.setTag("shouldMelt", String.valueOf(shouldMelt));
            op.finish();

            boolean melted = false;
            var fileToParse = input;
            if (shouldMelt) {
                op = span.startChild("melt");
                fileToParse = melter.melt(input);
                melted = true;
                isZipped = false;
                op.finish();
            }

            Status status = null;
            if (!isZipped) {
                op = span.startChild("parseText");
                status = parseText(fileToParse, melted, checksum);
            } else {
                op = span.startChild("parseCompressed");
                status = parseCompressed(fileToParse, checksum);
            }
            ISpan finalOp = op;
            status.visit(new StatusVisitor<SavegameInfo<?>>() {
                @Override
                public void success(Success<SavegameInfo<?>> s) {
                    finalOp.finish();
                }

                @Override
                public void error(Error e) {
                    finalOp.finish(SpanStatus.INTERNAL_ERROR);
                }

                @Override
                public void invalid(Invalid iv) {
                    finalOp.finish(SpanStatus.INVALID_ARGUMENT);
                }
            });
            return status;
        } catch (Throwable e) {
            span.finish(SpanStatus.INTERNAL_ERROR);
            return new Error(e);
        }
    }

    private static Status parseText(Path fileToParse, boolean melted, String checksum) throws Exception {
        try (var in = Files.newInputStream(fileToParse)) {
            boolean valid = FormatParser.validateHeader(EU4_TEXT_HEADER, in);
            if (!valid) {
                return new Invalid("Invalid header");
            }

            var content = in.readAllBytes();
            var node = TextFormatParser.eu4SavegameParser().parse(content);
            var info = Eu4SavegameInfo.fromSavegame(melted, node);
            if (info.getVersion().compareTo(MIN_VERSION) < 0) {
                return new Invalid("Savegame version " + info.getVersion() + " is not supported");
            }
            return new Success<>(false, checksum, node, info);
        }
    }

    private static Status parseCompressed(Path fileToParse, String checksum) throws Exception {
        try (var fs = FileSystems.newFileSystem(fileToParse)) {
            ArrayNode gamestateNode;
            ArrayNode metaNode;
            ArrayNode aiNode;

            var gs = fs.getPath("gamestate");
            if (!Files.exists(gs)) {
                return new Invalid("Missing gamestate. This might be a very old savegame, which is not supported");
            }
            var gsContent = Files.readAllBytes(gs);
            if (FormatParser.validateHeader(EU4_TEXT_HEADER, gsContent)) {
                gamestateNode = TextFormatParser.eu4SavegameParser().parse(gsContent);
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

            var node = new LinkedArrayNode(List.of(metaNode, gamestateNode, aiNode));
            var info = Eu4SavegameInfo.fromSavegame(false, node);
            if (info.getVersion().compareTo(MIN_VERSION) < 0) {
                return new Invalid("Savegame version " + info.getVersion() + " is not supported");
            }

            return new Success<>(true, checksum, node, info);
        }
    }
}

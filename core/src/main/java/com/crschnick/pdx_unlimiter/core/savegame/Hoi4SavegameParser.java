package com.crschnick.pdx_unlimiter.core.savegame;

import com.crschnick.pdx_unlimiter.core.info.hoi4.Hoi4SavegameInfo;
import com.crschnick.pdx_unlimiter.core.parser.FormatParser;
import com.crschnick.pdx_unlimiter.core.parser.TextFormatParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class Hoi4SavegameParser extends SavegameParser {

    private static final byte[] HOI4_TEXT_HEADER = "HOI4txt".getBytes(StandardCharsets.UTF_8);
    private static final byte[] HOI4_BINARY_HEADER = "HOI4bin".getBytes(StandardCharsets.UTF_8);


    public boolean isBinary(Path input) throws IOException {
        try (var in = Files.newInputStream(input)) {
            return FormatParser.validateHeader(HOI4_BINARY_HEADER, in);
        }
    }

    @Override
    public Status parse(Path input, Melter melter) {
        try {
            String checksum = checksum(Files.readAllBytes(input));

            boolean melted = false;
            var fileToParse = input;
            if (isBinary(input)) {
                fileToParse = melter.melt(input);
                melted = true;
            }

            try (var in = Files.newInputStream(fileToParse)) {
                boolean valid = FormatParser.validateHeader(HOI4_TEXT_HEADER, in);
                if (!valid) {
                    return new Invalid("Invalid header");
                }

                var content = in.readAllBytes();
                var node = TextFormatParser.hoi4SavegameParser().parse(content);
                var info = Hoi4SavegameInfo.fromSavegame(melted, node);
                return new Success<>(melted, checksum, node, info);
            }
        } catch (Throwable e) {
            return new Error(e);
        }
    }
}

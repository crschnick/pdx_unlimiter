package com.crschnick.pdxu.io.savegame;

import com.crschnick.pdxu.io.parser.ParseException;
import com.crschnick.pdxu.io.parser.TextFormatParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public class PlaintextSavegameStructure implements SavegameStructure {

    protected final byte[] header;
    private final String name;
    private final TextFormatParser parser;
    private final Function<SavegameContent, UUID> idExtractor;
    private final Consumer<SavegameContent> idGenerator;

    public PlaintextSavegameStructure(byte[] header, String name, TextFormatParser parser, Function<SavegameContent, UUID> idExtractor, Consumer<SavegameContent> idGenerator) {
        this.header = header;
        this.name = name;
        this.parser = parser;
        this.idExtractor = idExtractor;
        this.idGenerator = idGenerator;
    }

    @Override
    public void write(Path out, SavegameContent c) throws IOException {
        try (var partOut = Files.newOutputStream(out)) {
            if (header != null) {
                partOut.write(header);
                partOut.write("\n".getBytes());
            }
            writeData(partOut, c.get());
        }
    }

    @Override
    public UUID getCampaignIdHeuristic(SavegameContent c) {
        return idExtractor.apply(c);
    }

    @Override
    public void generateNewCampaignIdHeuristic(SavegameContent c) {
        idGenerator.accept(c);
    }

    @Override
    public SavegameParseResult parse(byte[] input) {
        if (header != null && !SavegameStructure.validateHeader(header, input)) {
            return new SavegameParseResult.Invalid("File " + name + " has an invalid header");
        }

        try {
            var node = parser.parse(input, header != null ? header.length + 1 : 0);
            return new SavegameParseResult.Success(Map.of(name, node));
        } catch (ParseException e) {
            return new SavegameParseResult.Error(e);
        }
    }

    @Override
    public TextFormatParser getParser() {
        return parser;
    }
}

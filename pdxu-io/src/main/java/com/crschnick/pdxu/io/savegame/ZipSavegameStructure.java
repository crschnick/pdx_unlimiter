package com.crschnick.pdxu.io.savegame;

import com.crschnick.pdxu.io.node.ArrayNode;
import com.crschnick.pdxu.io.node.NodeWriter;
import com.crschnick.pdxu.io.parser.TextFormatParser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipSavegameStructure implements SavegameStructure {

    private final byte[] header;
    private final TextFormatParser parser;
    private final Set<SavegamePart> parts;
    private final String[] ignored;
    private final Function<SavegameContent, UUID> idExtractor;
    private final Consumer<SavegameContent> idGenerator;

    public ZipSavegameStructure(byte[] header, TextFormatParser parser, Set<SavegamePart> parts, Function<SavegameContent, UUID> idExtractor, Consumer<SavegameContent> idGenerator, String... ignored) {
        this.header = header;
        this.parser = parser;
        this.parts = parts;
        this.idExtractor = idExtractor;
        this.idGenerator = idGenerator;
        this.ignored = ignored;
    }

    @Override
    public UUID getCampaignIdHeuristic(SavegameContent c) {
        return idExtractor.apply(c);
    }

    @Override
    public void generateNewCampaignIdHeuristic(SavegameContent c) {
        idGenerator.accept(c);
    }

    protected SavegameParseResult parseInput(byte[] input, int offset) {
        var wildcard = parts.stream()
                .filter(p -> p.identifier().equals("*"))
                .findAny();

        try {
            try (var zipIn = new ZipInputStream(new ByteArrayInputStream(input, offset, input.length - offset))) {
                Map<String, ArrayNode> nodes = new HashMap<>();
                ZipEntry entry;
                while ((entry = zipIn.getNextEntry()) != null) {
                    ZipEntry finalEntry = entry;

                    // Skip ignored entries
                    if (Arrays.stream(ignored).anyMatch(s -> s.equals(finalEntry.getName()))) {
                        continue;
                    }

                    var part = parts.stream()
                            .filter(p -> p.identifier().equals(finalEntry.getName()))
                            .findAny().or(() -> wildcard);

                    // Ignore unknown entry
                    if (part.isEmpty()) {
                        continue;
                    }

                    var bytes = zipIn.readAllBytes();
                    if (header != null && !SavegameStructure.validateHeader(header, bytes)) {
                        return new SavegameParseResult.Invalid("File " + part.get().identifier() + " has an invalid header");
                    }

                    var node = parser.parse(bytes, header != null ? header.length + 1 : 0);
                    nodes.put(part.get().name(), node);
                }

                var missingParts = parts.stream()
                        .map(part -> part.name())
                        .filter(s -> !nodes.containsKey(s))
                        .toList();
                if (missingParts.size() > 0) {
                    return new SavegameParseResult.Invalid("Missing parts: " + String.join(", ", missingParts));
                }

                return new SavegameParseResult.Success(nodes);
            }
        } catch (Throwable t) {
            return new SavegameParseResult.Error(t);
        }
    }

    @Override
    public void write(Path out, SavegameContent c) throws IOException {
        try (var fs = FileSystems.newFileSystem(out)) {
            for (var e : c.entrySet()) {
                var usedPart = parts.stream()
                        .filter(part -> part.name().equals(e.getKey()))
                        .findAny();
                if (usedPart.isEmpty()) {
                    continue;
                }

                var path = fs.getPath(usedPart.get().identifier());
                try (var partOut = Files.newOutputStream(path)) {
                    if (header != null) {
                        partOut.write(header);
                        partOut.write("\n".getBytes());
                    }
                    NodeWriter.write(partOut, parser.getCharset(), e.getValue(), "\t", 0);
                }
            }
        }
    }

    @Override
    public SavegameParseResult parse(byte[] input) {
        return parseInput(input, 0);
    }

    @Override
    public TextFormatParser getParser() {
        return parser;
    }

    public record SavegamePart(String name, String identifier) {
    }
}

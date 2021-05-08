package com.crschnick.pdx_unlimiter.core.savegame;

import com.crschnick.pdx_unlimiter.core.node.ArrayNode;
import com.crschnick.pdx_unlimiter.core.parser.TextFormatParser;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipSavegameStructure implements SavegameStructure {

    private final byte[] header;
    private final Charset charset;
    private final Set<SavegamePart> parts;
    private final String[] ignored;

    public ZipSavegameStructure(byte[] header, Charset charset, Set<SavegamePart> parts, String... ignored) {
        this.header = header;
        this.charset = charset;
        this.parts = parts;
        this.ignored = ignored;
    }

    protected SavegameParseResult parseInput(byte[] input, int offset) {
        var wildcard = parts.stream()
                .filter(p -> p.identifier().equals("*"))
                .findAny();

        try {
            try (var zipIn = new ZipInputStream(new ByteArrayInputStream(input, offset, Integer.MAX_VALUE))) {
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

                    var node = new TextFormatParser(charset).parse(bytes, header != null ? header.length + 1 : 0);
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
    public SavegameParseResult parse(byte[] input) {
        return parseInput(input, 0);
    }

    public record SavegamePart(String name, String identifier) {}


}

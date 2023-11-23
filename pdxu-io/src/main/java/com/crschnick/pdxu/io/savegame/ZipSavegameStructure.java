package com.crschnick.pdxu.io.savegame;

import com.crschnick.pdxu.io.node.ArrayNode;
import com.crschnick.pdxu.io.node.NodeWriter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipSavegameStructure implements SavegameStructure {

    public static byte[] getFirstHeader(byte[] input, int maxLength) {
        try {
            var zipIn = new ZipInputStream(new ByteArrayInputStream(input));
            if (zipIn.getNextEntry() != null) {
                return Arrays.copyOfRange(zipIn.readAllBytes(), 0, maxLength);
            }
        } catch (IOException ignored) {
        }

        return Arrays.copyOfRange(input, 0, maxLength);
    }

    private final byte[] header;
    private final SavegameType type;
    private final Set<SavegamePart> parts;
    private final String[] ignored;

    public ZipSavegameStructure(byte[] header,SavegameType type, Set<SavegamePart> parts, String... ignoredFiles) {
        this.header = header;
        this.type = type;
        this.parts = parts;
        this.ignored = ignoredFiles;
    }

    protected SavegameParseResult parseInput(byte[] input, int offset) {
        var wildcard = parts.stream()
                .filter(p -> p.fileName().equals("*"))
                .findAny();

        try {
            try (var zipIn = new ZipInputStream(new ByteArrayInputStream(input, offset, input.length - offset))) {
                Map<String, ArrayNode> nodes = new LinkedHashMap<>();
                ZipEntry entry;
                while ((entry = zipIn.getNextEntry()) != null) {
                    ZipEntry finalZipEntry = entry;

                    // Skip ignored entries
                    if (Arrays.stream(ignored).anyMatch(s -> s.equals(finalZipEntry.getName()))) {
                        continue;
                    }

                    var part = parts.stream()
                            .filter(p -> p.fileName().equals(finalZipEntry.getName()))
                            .findAny().or(() -> wildcard);

                    // Ignore unknown entry
                    if (part.isEmpty()) {
                        continue;
                    }

                    var bytes = zipIn.readAllBytes();
                    if (header != null && !SavegameStructure.validateHeader(header, bytes)) {
                        return new SavegameParseResult.Invalid("File " + part.get().identifier() + " has an invalid header");
                    }

                    var node = type.getParser().parse(part.get().identifier(), bytes, header != null ? header.length + 1 : 0);
                    if (node.size() == 0) {
                        return new SavegameParseResult.Invalid("File " + part.get().identifier() + " is empty");
                    }

                    nodes.put(part.get().identifier(), node);
                }

                var missingParts = parts.stream()
                        .map(SavegamePart::identifier)
                        .filter(s -> !nodes.containsKey(s))
                        .toList();
                if (missingParts.size() > 0) {
                    return new SavegameParseResult.Invalid("Missing parts: " + String.join(", ", missingParts));
                }

                return new SavegameParseResult.Success(new SavegameContent(nodes));
            }
        } catch (Exception t) {
            return new SavegameParseResult.Error(t);
        }
    }

    @Override
    public void write(Path out, SavegameContent content) throws IOException {
        try (var fs = FileSystems.newFileSystem(out, Map.of("create", true))) {
            Optional<SavegamePart> wildcardPart;
            try (var list = Files.list(fs.getPath("/"))) {
                 wildcardPart = list.map(path -> path.getFileName().toString()).filter(p -> content.entrySet().stream().noneMatch(e -> p.equals(e.getKey())))
                         .map(s -> new SavegamePart(s, "gamestate"))
                        .findAny();
            }

            for (var e : content.entrySet()) {
                var usedPart = parts.stream()
                        .filter(part -> part.fileName().equals(e.getKey()))
                        .findAny();

                if (usedPart.isEmpty() && wildcardPart.isPresent() && wildcardPart.get().identifier().equals(e.getKey())) {
                    usedPart = wildcardPart;
                }

                if (usedPart.isEmpty()) {
                    continue;
                }

                var path = fs.getPath(usedPart.get().fileName());
                try (var partOut = Files.newOutputStream(path)) {
                    if (header != null) {
                        partOut.write(header);
                        partOut.write("\n".getBytes());
                    }
                    NodeWriter.write(partOut, type.getParser().getCharset(), e.getValue(), "\t", 0);
                }
            }
        }
    }

    @Override
    public SavegameParseResult parse(byte[] input) {
        return parseInput(input, 0);
    }

    @Override
    public SavegameType getType() {
        return type;
    }

    public record SavegamePart(String fileName, String identifier) {
    }
}

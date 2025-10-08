package com.crschnick.pdxu.app.installation;

import com.crschnick.pdxu.app.issue.ErrorEventFactory;
import com.crschnick.pdxu.app.util.JacksonMapper;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.parser.TextFormatParser;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class GameMod {

    private Path modFile;
    private Path path;
    private String name;
    private boolean legacyArchive;

    public static Optional<GameMod> fromVictoria3Directory(Path p) {
        if (!Files.exists(p.resolve(".metadata").resolve("metadata.json"))) {
            return Optional.empty();
        }

        GameMod mod = new GameMod();
        mod.modFile = p.resolve(".metadata").resolve("metadata.json");

        if (!p.toFile().canRead()) {
            return Optional.of(mod);
        }

        try {
            var content = JacksonMapper.getDefault().readTree(mod.modFile.toFile());

            // Quick check if mod data seems valid
            if (content.get("name") == null) {
                return Optional.of(mod);
            }

            mod.name = content.get("name").asText();
            mod.path = p;
            return Optional.of(mod);
        } catch (Exception ex) {
            // Don't report mod parsing errors
            ErrorEventFactory.fromThrowable("Could not parse malformed mod file " + p.toString(), ex).omit().handle();
        }

        return Optional.of(mod);
    }

    public static Optional<GameMod> fromFile(Path p) {
        if (!p.getFileName().toString().endsWith(".mod")) {
            return Optional.empty();
        }

        GameMod mod = new GameMod();
        mod.modFile = p;

        if (!p.toFile().canRead()) {
            return Optional.of(mod);
        }

        try {
            Node node = TextFormatParser.text().parse(p);

            // Quick check if mod data seems valid
            if (node.getNodeForKeyIfExistent("name").isEmpty()) {
                return Optional.of(mod);
            }

            mod.name = node.getNodeForKey("name").getString();
            var path = node.getNodeForKeyIfExistent("path");
            if (path.isEmpty()) {
                var ar = node.getNodeForKeyIfExistent("archive");
                if (ar.isPresent()) {
                    mod.legacyArchive = true;
                    // Sometimes, mod paths are messed up with a missing end quote
                    mod.path = Path.of(ar.get().getString().trim().replace("\"", ""));
                } else {
                    return Optional.empty();
                }
            } else {
                // Sometimes, mod paths are messed up with a missing end quote
                mod.path = Path.of(path.get().getString().trim().replace("\"", ""));
            }

            return Optional.of(mod);
        } catch (Exception ex) {
            // Don't report mod parsing errors
            ErrorEventFactory.fromThrowable("Could not parse malformed mod file " + p.toString(), ex).omit().handle();
        }

        return Optional.of(mod);
    }

    public Path getModFile() {
        return modFile;
    }

    public Optional<Path> getAbsoluteContentPath(Path base) {
        return getContentPath().map(base::resolve);
    }

    public Optional<Path> getContentPath() {
        return Optional.ofNullable(path);
    }

    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

    public boolean isLegacyArchive() {
        return legacyArchive;
    }
}

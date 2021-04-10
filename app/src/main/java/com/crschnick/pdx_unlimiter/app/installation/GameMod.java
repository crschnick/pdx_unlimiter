package com.crschnick.pdx_unlimiter.app.installation;

import com.crschnick.pdx_unlimiter.core.node.Node;
import com.crschnick.pdx_unlimiter.core.parser.TextFormatParser;

import java.nio.file.Path;
import java.util.Optional;

public class GameMod {

    private Path modFile;
    private Path path;
    private String name;
    private String supportedVersion;
    private boolean legacyArchive;

    public static Optional<GameMod> fromFile(Path p) throws Exception {
        if (!p.getFileName().toString().endsWith(".mod")) {
            return Optional.empty();
        }

        Node node = TextFormatParser.textFileParser().parse(p);
        // Quick check if mod data seems valid
        if (node.getNodeForKeyIfExistent("name").isEmpty()) {
            return Optional.empty();
        }

        GameMod mod = new GameMod();
        mod.modFile = p;
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

        mod.supportedVersion = node.getNodeForKeyIfExistent("supported_version").map(Node::getString).orElse("*");
        return Optional.of(mod);
    }

    public Path getModFile() {
        return modFile;
    }

    public Path getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public String getSupportedVersion() {
        return supportedVersion;
    }

    public boolean isLegacyArchive() {
        return legacyArchive;
    }
}

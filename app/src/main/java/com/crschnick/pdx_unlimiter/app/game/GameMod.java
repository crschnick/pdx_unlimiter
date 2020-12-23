package com.crschnick.pdx_unlimiter.app.game;

import com.crschnick.pdx_unlimiter.core.parser.Node;
import com.crschnick.pdx_unlimiter.core.parser.TextFormatParser;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class GameMod {

    private Path modFile;
    private Path path;
    private String name;
    private String supportedVersion;

    public static Optional<GameMod> fromFile(Path p) throws Exception {
        if (!p.getFileName().toString().endsWith(".mod")) {
            return Optional.empty();
        }

        Node node = TextFormatParser.textFileParser().parse(Files.newInputStream(p));
        GameMod mod = new GameMod();
        mod.modFile = p;
        mod.name = node.getNodeForKey("name").getString();
        var path = node.getNodeForKeyIfExistent("path");
        if (path.isEmpty()) {
            return Optional.empty();
        }

        mod.path = Path.of(path.get().getString());
        mod.supportedVersion = node.getNodeForKey("supported_version").getString();
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
}

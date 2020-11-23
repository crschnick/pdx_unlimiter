package com.crschnick.pdx_unlimiter.app.game;

import com.crschnick.pdx_unlimiter.eu4.parser.Eu4NormalParser;
import com.crschnick.pdx_unlimiter.eu4.parser.Node;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class GameMod {

    public static Optional<GameMod> fromFile(Path p) throws IOException {
        if (!p.getFileName().toString().endsWith(".mod")) {
            return Optional.empty();
        }

        Node node = Eu4NormalParser.textFileParser().parse(Files.newInputStream(p)).get();
        GameMod mod = new GameMod();
        mod.name = Node.getString(Node.getNodeForKey(node, "name"));
        var path = Node.getNodeForKeyIfExistent(node, "path");
        if (path.isEmpty()) {
            return Optional.empty();
        }

        mod.path = Path.of(Node.getString(path.get()));
        mod.supportedVersion = Node.getString(Node.getNodeForKey(node, "supported_version"));
        return Optional.of(mod);
    }

    private Path path;
    private String name;
    private String supportedVersion;

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

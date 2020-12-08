package com.crschnick.pdx_unlimiter.app.game;

import com.crschnick.pdx_unlimiter.core.parser.Node;
import com.crschnick.pdx_unlimiter.core.parser.TextFormatParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class GameDlc {

    private boolean expansion;
    private Path filePath;
    private Path dataPath;
    private String name;
    private boolean affectsChecksum;
    private boolean affectsCompatability;

    public static Optional<GameDlc> fromDirectory(Path p) throws IOException {
        if (!Files.isDirectory(p)) {
            return Optional.empty();
        }

        String dlcName = p.getFileName().toString();
        String dlcId = dlcName.split("_")[0];
        Path filePath = p.resolve(dlcId + ".dlc");
        Path dataPath = p.resolve(dlcId + ".zip");

        if (!Files.exists(filePath)) {
            return Optional.empty();
        }

        Node node = TextFormatParser.textFileParser().parse(Files.newInputStream(filePath)).get();
        GameDlc dlc = new GameDlc();
        dlc.expansion = Node.getString(Node.getNodeForKey(node, "category")).equals("expansion");
        dlc.filePath = p.getParent().relativize(filePath);
        dlc.dataPath = p.getParent().relativize(dataPath);
        dlc.name = Node.getString(Node.getNodeForKey(node, "name"));
        dlc.affectsChecksum = Node.getBoolean(Node.getNodeForKey(node, "affects_checksum"));
        dlc.affectsCompatability = Node.getBoolean(Node.getNodeForKey(node, "affects_compatability"));
        return Optional.of(dlc);
    }

    public boolean isExpansion() {
        return expansion;
    }

    public Path getInfoFilePath() {
        return filePath;
    }

    public String getName() {
        return name;
    }

    public Path getDataPath() {
        return dataPath;
    }

    public boolean isAffectsChecksum() {
        return affectsChecksum;
    }

    public boolean isAffectsCompatability() {
        return affectsCompatability;
    }
}

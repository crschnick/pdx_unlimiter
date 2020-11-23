package com.crschnick.pdx_unlimiter.app.game;

import com.crschnick.pdx_unlimiter.eu4.parser.Eu4NormalParser;
import com.crschnick.pdx_unlimiter.eu4.parser.Node;
import javafx.scene.image.Image;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class GameDlc {

    public static Optional<GameDlc> fromDirectory(Path p) throws IOException {
        if (!Files.isDirectory(p)) {
            return Optional.empty();
        }

        String dlcName = p.getFileName().toString();
        String dlcId = dlcName.split("_")[0];
        Path filePath = p.resolve(dlcId + ".dlc");

        if (!Files.exists(filePath)) {
            return Optional.empty();
        }

        Node node = Eu4NormalParser.textFileParser().parse(Files.newInputStream(filePath)).get();
        GameDlc dlc = new GameDlc();
        dlc.filePath = p.getParent().relativize(filePath);
        dlc.name = Node.getString(Node.getNodeForKey(node, "name"));
        dlc.affectsChecksum = Node.getBoolean(Node.getNodeForKey(node, "affects_checksum"));
        dlc.affectsCompatability = Node.getBoolean(Node.getNodeForKey(node, "affects_compatability"));
        return Optional.of(dlc);
    }

    private Path filePath;
    private String name;
    private boolean affectsChecksum;
    private boolean affectsCompatability;

    public Path getFilePath() {
        return filePath;
    }

    public String getName() {
        return name;
    }

    public boolean isAffectsChecksum() {
        return affectsChecksum;
    }

    public boolean isAffectsCompatability() {
        return affectsCompatability;
    }
}

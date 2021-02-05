package com.crschnick.pdx_unlimiter.app.installation;

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

        Node node = TextFormatParser.textFileParser().parse(filePath);
        GameDlc dlc = new GameDlc();
        dlc.expansion = node.getNodeForKeyIfExistent("category")
                .map(n -> n.getString().equals("expansion"))
                .orElse(false);
        dlc.filePath = filePath;
        dlc.dataPath = dataPath;
        dlc.name = node.getNodeForKey("name").getString();
        dlc.affectsChecksum = node.getNodeForKey("affects_checksum").getBoolean();
        dlc.affectsCompatability = node.getNodeForKeyIfExistent("affects_compatability")
                .map(Node::getBoolean)
                .orElse(false);
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
}

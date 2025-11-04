package com.crschnick.pdxu.app.installation;

import com.crschnick.pdxu.app.util.JacksonMapper;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.parser.TextFormatParser;

import lombok.Getter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class GameDlc {

    private Path filePath;
    private String name;

    @Getter
    private boolean affectsCompatibility;

    @Override
    public String toString() {
        return name;
    }

    public static Optional<GameDlc> fromDirectory(Path p) throws Exception {
        if (!Files.isDirectory(p)) {
            return Optional.empty();
        }

        String dlcName = p.getFileName().toString();
        String dlcId = dlcName.split("_")[0];
        Path filePath =
                Files.exists(p.resolve(dlcId + ".dlc")) ? p.resolve(dlcId + ".dlc") : p.resolve(dlcName + ".dlc");

        if (!Files.exists(filePath)) {
            return parseJsonDlc(p);
        }

        Node node = TextFormatParser.text().parse(filePath);
        GameDlc dlc = new GameDlc();
        dlc.filePath = filePath;
        dlc.name = node.getNodeForKey("name").getString();

        // Notice the misspelled word "compatability"!
        if (node.getNodeForKeyIfExistent("affects_compatability")
                .map(Node::getBoolean)
                .orElse(false)) {
            dlc.affectsCompatibility = true;
        }

        // In this version it is no longer misspelled!
        if (node.getNodeForKeyIfExistent("affects_save_compatibility")
                .map(Node::getBoolean)
                .orElse(false)) {
            dlc.affectsCompatibility = true;
        }

        if (!node.hasKey("affects_compatability") && !node.hasKey("affects_save_compatibility")) {
            dlc.affectsCompatibility = true;
        }

        return Optional.of(dlc);
    }

    private static Optional<GameDlc> parseJsonDlc(Path p) throws Exception {
        String dlcName = p.getFileName().toString();
        Path filePath = p.resolve(dlcName + ".dlc.json");
        if (!Files.exists(filePath)) {
            return Optional.empty();
        }

        var json = JacksonMapper.getDefault().readTree(filePath.toFile());
        GameDlc dlc = new GameDlc();
        dlc.filePath = filePath;
        dlc.name = json.required("name").asText();
        dlc.affectsCompatibility = json.required("affects_save_compatibility").asBoolean();
        return Optional.of(dlc);
    }

    public Path getInfoFilePath() {
        return filePath;
    }

    public String getName() {
        return name;
    }
}

package com.crschnick.pdxu.io.savegame;

import com.crschnick.pdxu.io.node.ArrayNode;
import com.crschnick.pdxu.io.node.NodeWriter;
import lombok.Value;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@Value
public class ModernPlaintextSavegameStructure implements SavegameStructure {

    SavegameType type;

    @Override
    public void write(Path out, SavegameContent content) throws IOException {
        var gamestate = content.get("gamestate");
        ArrayNode meta = (ArrayNode) gamestate.getNodeForKey("meta_data");
        var metaHeaderNode = ArrayNode.singleKeyNode("meta_data", meta);

        try (var gsOut = Files.newOutputStream(out)) {
            var metaBytes = NodeWriter.writeToBytes(metaHeaderNode, Integer.MAX_VALUE, "\t");
            // Exclude trailing new line in meta length!
            String header = new ModernHeader(true, 0, false, metaBytes.length - 1).toString();
            gsOut.write((header + "\n").getBytes(StandardCharsets.UTF_8));

            NodeWriter.write(gsOut, StandardCharsets.UTF_8, gamestate, "\t", 0);
        }
    }

    @Override
    public SavegameParseResult parse(byte[] input) {
        int metaStart;
        if (ModernHeader.skipsHeader(input)) {
            metaStart = 0;
        } else {
            var header = ModernHeader.determineHeaderForFile(input);
            if (header.binary()) {
                throw new IllegalArgumentException("Binary savegames are not supported");
            }
            if (header.isCompressed()) {
                throw new IllegalArgumentException("Compressed savegames are not supported");
            }

            metaStart = header.toString().length() + 1;
        }
        try {
            var node = getType().getParser().parse("gamestate", input, metaStart);
            if (node.size() == 0) {
                return new SavegameParseResult.Invalid("File gamestate is empty");
            }

            return new SavegameParseResult.Success(new SavegameContent(Map.of("gamestate", node)));
        } catch (Exception t) {
            return new SavegameParseResult.Error(t);
        }
    }

    @Override
    public SavegameType getType() {
        return type;
    }
}

package com.crschnick.pdxu.io.savegame;

import com.crschnick.pdxu.io.node.ArrayNode;
import com.crschnick.pdxu.io.node.NodeWriter;
import com.crschnick.pdxu.io.node.ValueNode;
import com.crschnick.pdxu.io.parser.TextFormatParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class Ck3PlaintextSavegameStructure implements SavegameStructure {

    @Override
    public void write(Path out, SavegameContent c) throws IOException {
        var gamestate = c.get("gamestate");
        ArrayNode meta = (ArrayNode) gamestate.getNodeForKey("meta_data");
        var metaHeaderNode = ArrayNode.singleKeyNode("meta_data", meta);

        try (var gsOut = Files.newOutputStream(out)) {
            var metaBytes = NodeWriter.writeToBytes(metaHeaderNode, Integer.MAX_VALUE, "\t");
            // Exclude trailing new line in meta length!
            String header = new Ck3Header(false, false, metaBytes.length - 1).toString();
            gsOut.write((header + "\n").getBytes(StandardCharsets.UTF_8));

            NodeWriter.write(gsOut, StandardCharsets.UTF_8, gamestate, "\t", 0);
        }
    }

    @Override
    public UUID getCampaignIdHeuristic(SavegameContent c) {
        long seed = c.get().getNodeForKey("random_seed").getLong();
        byte[] b = new byte[20];
        new Random(seed).nextBytes(b);
        return UUID.nameUUIDFromBytes(b);
    }

    @Override
    public void generateNewCampaignIdHeuristic(SavegameContent c) {
        int rand = new Random().nextInt(Integer.MAX_VALUE);
        c.get().getNodeForKey("random_seed").getValueNode().set(
                new ValueNode(String.valueOf(rand), false));
    }

    @Override
    public SavegameParseResult parse(byte[] input) {
        var header = Ck3Header.fromStartOfFile(input);
        if (header.binary()) {
            throw new IllegalArgumentException("Binary savegames are not supported");
        }
        if (header.compressed()) {
            throw new IllegalArgumentException("Compressed savegames are not supported");
        }

        int metaStart = header.toString().length() + 1;
        try {
            var node = getParser().parse(input, metaStart);
            return new SavegameParseResult.Success(Map.of("gamestate", node));
        } catch (Throwable t) {
            return new SavegameParseResult.Error(t);
        }
    }

    @Override
    public TextFormatParser getParser() {
        return TextFormatParser.CK3;
    }
}

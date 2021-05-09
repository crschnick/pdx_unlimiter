package com.crschnick.pdx_unlimiter.core.savegame;

import com.crschnick.pdx_unlimiter.core.node.ArrayNode;
import com.crschnick.pdx_unlimiter.core.writer.NodeWriter;
import com.crschnick.pdx_unlimiter.core.parser.TextFormatParser;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class Ck3PlaintextSavegameStructure implements SavegameStructure {

    @Override
    public void write(Path out, Map<String, ArrayNode> nodes) throws IOException {
        var gamestate = nodes.get("gamestate");
        ArrayNode meta = (ArrayNode) gamestate.getNodeForKey("meta_data");
        var metaHeaderNode = ArrayNode.singleKeyNode("meta_data", meta);

        try (var gsOut = Files.newOutputStream(out)) {
            var metaBytes = NodeWriter.writeToBytes(metaHeaderNode, Integer.MAX_VALUE, "\t");
            // Exclude trailing new line in meta length!
            String header = new Ck3Header(false, false, metaBytes.length - 1).toString();
            gsOut.write((header + "\n").getBytes(StandardCharsets.UTF_8));

            NodeWriter.write(gsOut, StandardCharsets.UTF_8, gamestate, "\t");
        }
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
            var node = new TextFormatParser(StandardCharsets.UTF_8)
                    .parse(input, metaStart);
            return new SavegameParseResult.Success(Map.of("gamestate", node));
        } catch (Throwable t) {
            return new SavegameParseResult.Error(t);
        }
    }

    @Override
    public Charset getCharset() {
        return StandardCharsets.UTF_8;
    }
}

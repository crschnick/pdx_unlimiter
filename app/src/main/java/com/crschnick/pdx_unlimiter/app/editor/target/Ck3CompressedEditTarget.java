package com.crschnick.pdx_unlimiter.app.editor.target;

import com.crschnick.pdx_unlimiter.core.savegame.Ck3Header;
import com.crschnick.pdx_unlimiter.core.info.ck3.Ck3SavegameInfo;
import com.crschnick.pdx_unlimiter.core.node.ArrayNode;
import com.crschnick.pdx_unlimiter.core.node.LinkedArrayNode;
import com.crschnick.pdx_unlimiter.core.node.Node;
import com.crschnick.pdx_unlimiter.core.parser.NodeWriter;
import com.crschnick.pdx_unlimiter.core.parser.TextFormatParser;
import com.crschnick.pdx_unlimiter.core.savegame.Ck3SavegameParser;
import com.crschnick.pdx_unlimiter.core.savegame.SavegameParseResult;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Ck3CompressedEditTarget extends EditTarget {

    public Ck3CompressedEditTarget(Path file) {
        super(file, TextFormatParser.ck3SavegameParser());
    }

    @Override
    public Map<String, Node> parse() throws Exception {
        var s = new Ck3SavegameParser().parse(file, null);
        Map<String, Node> map = new HashMap<>();
        s.visit(new SavegameParseResult.Visitor<Ck3SavegameInfo>() {
            @Override
            public void success(SavegameParseResult.Success<Ck3SavegameInfo> s) {
                map.put("gamestate", s.content);
            }
        });

        if (map.size() == 0) {
            throw new IllegalArgumentException();
        }

        return map;
    }

    @Override
    public void write(Map<String, Node> nodeMap) throws Exception {
        write(file, (ArrayNode) nodeMap.get("gamestate"));
    }

    private void write(Path file, ArrayNode gamestate) throws IOException {
        ArrayNode meta = (ArrayNode) gamestate.getNodeForKey("meta_data");
        var metaHeaderNode = ArrayNode.singleKeyNode("meta_data", meta);

        try (var out = Files.newOutputStream(file)) {
            var metaBytes = NodeWriter.writeToBytes(metaHeaderNode, Integer.MAX_VALUE, "\t");

            // Exclude trailing new line in meta length!
            String header = new Ck3Header(true, false, metaBytes.length - 1).toString();
            out.write((header + "\n").getBytes(StandardCharsets.UTF_8));
            out.write(metaBytes);
            try (var zout = new ZipOutputStream(out)) {
                zout.putNextEntry(new ZipEntry("gamestate"));
                NodeWriter.write(zout, StandardCharsets.UTF_8, new LinkedArrayNode(List.of(metaHeaderNode, gamestate)), "\t");
                zout.closeEntry();
            }
        }
    }
}

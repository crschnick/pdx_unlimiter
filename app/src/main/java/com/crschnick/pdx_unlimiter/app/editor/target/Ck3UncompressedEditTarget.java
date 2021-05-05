package com.crschnick.pdx_unlimiter.app.editor.target;

import com.crschnick.pdx_unlimiter.core.info.ck3.Ck3SavegameInfo;
import com.crschnick.pdx_unlimiter.core.node.ArrayNode;
import com.crschnick.pdx_unlimiter.core.node.Node;
import com.crschnick.pdx_unlimiter.core.parser.TextFormatParser;
import com.crschnick.pdx_unlimiter.core.savegame.Ck3SavegameParser;
import com.crschnick.pdx_unlimiter.core.savegame.SavegameParser;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class Ck3UncompressedEditTarget extends EditTarget {

    public Ck3UncompressedEditTarget(Path file) {
        super(file, TextFormatParser.ck3SavegameParser());
    }

    @Override
    public Map<String, Node> parse() throws Exception {
        var s = new Ck3SavegameParser().parse(file, null);
        Map<String, Node> map = new HashMap<>();
        s.visit(new SavegameParser.StatusVisitor<Ck3SavegameInfo>() {
            @Override
            public void success(SavegameParser.Success<Ck3SavegameInfo> s) {
                map.put("root", s.content);
            }
        });

        if (map.size() == 0) {
            throw new IllegalArgumentException();
        }

        return map;
    }

    @Override
    public void write(Map<String, Node> nodeMap) throws Exception {
        Ck3SavegameParser.writeUncompressedPlaintext(file, (ArrayNode) nodeMap.get("root"));
    }
}

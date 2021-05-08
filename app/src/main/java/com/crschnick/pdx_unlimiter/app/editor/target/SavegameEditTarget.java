package com.crschnick.pdx_unlimiter.app.editor.target;

import com.crschnick.pdx_unlimiter.core.node.ArrayNode;
import com.crschnick.pdx_unlimiter.core.savegame.SavegameParseResult;
import com.crschnick.pdx_unlimiter.core.savegame.SavegameStructure;
import com.crschnick.pdx_unlimiter.core.savegame.SavegameType;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class SavegameEditTarget extends EditTarget {

    protected final SavegameType type;
    private SavegameStructure structure;

    public SavegameEditTarget(Path file, SavegameType type) {
        super(file);
        this.type = type;
    }

    @Override
    public Map<String, ArrayNode> parse() throws Exception {
        var bytes = Files.readAllBytes(file);
        structure = type.determineStructure(bytes);

        final Map<String, ArrayNode>[] map = new Map[]{null};
        structure.parse(bytes).visit(new SavegameParseResult.Visitor() {
            @Override
            public void success(SavegameParseResult.Success s) {
                map[0] = s.content;
            }
        });

        if (map[0] == null) {
            throw new IllegalArgumentException();
        }

        return map[0];
    }

    @Override
    public void write(Map<String, ArrayNode> nodeMap) throws Exception {
        structure.write(file, nodeMap);
    }
}

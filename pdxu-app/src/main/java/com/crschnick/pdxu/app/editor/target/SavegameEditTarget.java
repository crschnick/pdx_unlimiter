package com.crschnick.pdxu.app.editor.target;

import com.crschnick.pdxu.app.installation.GameFileContext;
import com.crschnick.pdxu.io.node.ArrayNode;
import com.crschnick.pdxu.io.parser.TextFormatParser;
import com.crschnick.pdxu.io.savegame.SavegameStructure;
import com.crschnick.pdxu.io.savegame.SavegameType;

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
    public boolean isSavegame() {
        return true;
    }

    @Override
    public Map<String, ArrayNode> parse() throws Exception {
        var bytes = Files.readAllBytes(file);
        structure = type.determineStructure(bytes);

        var succ = structure.parse(bytes).success();
        if (succ.isPresent()) {
            return succ.get().content;
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void write(Map<String, ArrayNode> nodeMap) throws Exception {
        structure.write(file, nodeMap);
    }

    @Override
    public TextFormatParser getParser() {
        return structure.getType().getParser();
    }

    @Override
    public String getName() {
        return file.getFileName().toString();
    }

    @Override
    public GameFileContext getFileContext() {
        return GameFileContext.forType(type);
    }
}

package com.crschnick.pdxu.app.editor.target;

import com.crschnick.pdxu.app.core.SavegameManagerState;
import com.crschnick.pdxu.app.installation.GameFileContext;
import com.crschnick.pdxu.io.node.ArrayNode;
import com.crschnick.pdxu.io.parser.TextFormatParser;
import com.crschnick.pdxu.io.savegame.SavegameType;

import java.nio.file.Path;
import java.util.Map;

public class ExternalEditTarget extends EditTarget {

    private final EditTarget target;

    public ExternalEditTarget(Path file) {
        super(file);

        SavegameType t = SavegameType.getTypeForFile(file);
        if (t != null) {
            target = new SavegameEditTarget(file, t);
        } else {
            target = new DataFileEditTarget(GameFileContext.forGame(SavegameManagerState.get().current()), file);
        }
    }

    @Override
    public boolean isSavegame() {
        return target.isSavegame();
    }

    @Override
    public Map<String, ArrayNode> parse() throws Exception {
        return target.parse();
    }

    @Override
    public void write(Map<String, ArrayNode> nodeMap) throws Exception {
        target.write(nodeMap);
    }

    @Override
    public TextFormatParser getParser() {
        return target.getParser();
    }

    @Override
    public String getName() {
        return target.getName();
    }

    @Override
    public GameFileContext getFileContext() {
        return target.getFileContext();
    }
}

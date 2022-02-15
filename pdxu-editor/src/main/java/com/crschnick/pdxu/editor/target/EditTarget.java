package com.crschnick.pdxu.editor.target;

import com.crschnick.pdxu.app.installation.GameFileContext;
import com.crschnick.pdxu.io.node.ArrayNode;
import com.crschnick.pdxu.io.parser.TextFormatParser;
import com.crschnick.pdxu.io.savegame.SavegameContent;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public abstract class EditTarget {

    protected final Path file;

    public EditTarget(Path file) {
        this.file = file;
    }

    public boolean canSave() {
        return Files.isWritable(file);
    }

    public abstract boolean isSavegame();

    public abstract SavegameContent parse() throws Exception;

    public abstract void write(Map<String, ArrayNode> nodeMap) throws Exception;

    public abstract TextFormatParser getParser();

    public Path getFile() {
        return file;
    }

    public abstract String getName();

    public abstract GameFileContext getFileContext();
}

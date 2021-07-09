package com.crschnick.pdxu.editor.target;

import com.crschnick.pdxu.app.installation.GameFileContext;
import com.crschnick.pdxu.io.node.ArrayNode;
import com.crschnick.pdxu.io.parser.TextFormatParser;
import com.crschnick.pdxu.io.savegame.SavegameType;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

public abstract class EditTarget {

    protected final Path file;

    public EditTarget(Path file) {
        this.file = file;
    }

    public static Optional<EditTarget> create(Path file) {
        SavegameType type = SavegameType.getTypeForFile(file);
        if (type == null) {
            return Optional.empty();
        }

        return Optional.of(new SavegameEditTarget(file, type));
    }

    public boolean canSave() {
        return Files.isWritable(file);
    }

    public abstract boolean isSavegame();

    public abstract Map<String, ArrayNode> parse() throws Exception;

    public abstract void write(Map<String, ArrayNode> nodeMap) throws Exception;

    public abstract TextFormatParser getParser();

    public Path getFile() {
        return file;
    }

    public abstract String getName();

    public abstract GameFileContext getFileContext();
}

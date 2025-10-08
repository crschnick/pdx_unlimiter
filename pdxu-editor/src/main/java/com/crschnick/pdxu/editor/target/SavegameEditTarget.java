package com.crschnick.pdxu.editor.target;

import com.crschnick.pdxu.app.installation.GameFileContext;
import com.crschnick.pdxu.app.util.RakalyHelper;
import com.crschnick.pdxu.io.node.ArrayNode;
import com.crschnick.pdxu.io.parser.TextFormatParser;
import com.crschnick.pdxu.io.savegame.SavegameContent;
import com.crschnick.pdxu.io.savegame.SavegameStructure;
import com.crschnick.pdxu.io.savegame.SavegameType;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class SavegameEditTarget extends EditTarget {

    protected final SavegameType type;
    private SavegameStructure structure;
    private boolean binary;

    public SavegameEditTarget(Path file, SavegameType type) {
        super(file);
        this.type = type;
    }

    @Override
    public boolean isSavegame() {
        return true;
    }

    @Override
    public boolean canSave() {
        return super.canSave() && !binary;
    }

    @Override
    public SavegameContent parse() throws Exception {
        var bytes = Files.readAllBytes(file);
        binary = type.isBinary(bytes);
        if (type.isBinary(bytes)) {
            bytes = RakalyHelper.toEquivalentPlaintext(file);
        }

        structure = type.determineStructure(bytes);
        var res = structure.parse(bytes);
        var succ = res.success();
        if (succ.isPresent()) {
            return succ.get().content;
        } else {
            var msg = res.error().map(e -> e.error.getMessage())
                    .or(() -> res.invalid().map(i -> i.message))
                    .orElse("");
            throw new IllegalArgumentException(msg);
        }
    }

    @Override
    public void write(Map<String, ArrayNode> nodeMap) throws Exception {
        structure.write(file, new SavegameContent(nodeMap));
    }

    @Override
    public TextFormatParser getParser() {
        return structure.getType().getParser();
    }

    @Override
    public String getName() {
        return file.getFileName().toString() + (binary ? " (Binary/Read-only)" : "");
    }

    @Override
    public GameFileContext getFileContext() {
        return GameFileContext.forType(type);
    }
}

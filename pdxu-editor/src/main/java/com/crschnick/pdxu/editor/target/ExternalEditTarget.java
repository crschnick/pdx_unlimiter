package com.crschnick.pdxu.editor.target;

import com.crschnick.pdxu.app.core.AppLayoutModel;
import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.app.installation.GameFileContext;
import com.crschnick.pdxu.app.installation.GameInstallation;
import com.crschnick.pdxu.io.node.ArrayNode;
import com.crschnick.pdxu.io.parser.TextFormatParser;
import com.crschnick.pdxu.io.savegame.SavegameContent;
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
            target = new DataFileEditTarget(determineContext(file), file);
        }
    }

    private GameFileContext determineContext(Path file) {
        for (Game g : Game.values()) {
            if (!g.isEnabled()) {
                continue;
            }

            if (file.startsWith(GameInstallation.ALL.get(g).getInstallDir()) ||
                    file.startsWith(GameInstallation.ALL.get(g).getUserDir())) {
                return GameFileContext.forGame(g);
            }
        }

        var current = AppLayoutModel.get().getActiveGame();
        if (current.isPresent()) {
            return GameFileContext.forGame(current.get());
        }

        var first = GameInstallation.ALL.mapIterator().getKey();
        return GameFileContext.forGame(first);
    }

    @Override
    public boolean canSave() {
        return target.canSave();
    }

    @Override
    public boolean isSavegame() {
        return target.isSavegame();
    }

    @Override
    public SavegameContent parse() throws Exception {
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

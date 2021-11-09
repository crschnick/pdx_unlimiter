package com.crschnick.pdxu.editor.target;

import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.app.installation.GameFileContext;
import com.crschnick.pdxu.app.savegame.SavegameActions;
import com.crschnick.pdxu.app.savegame.SavegameEntry;
import com.crschnick.pdxu.app.savegame.SavegameStorage;
import com.crschnick.pdxu.io.node.ArrayNode;
import com.crschnick.pdxu.io.parser.TextFormatParser;
import com.crschnick.pdxu.io.savegame.SavegameContent;
import com.crschnick.pdxu.model.SavegameInfo;

import java.util.Map;

public class StorageEditTarget<T, I extends SavegameInfo<T>> extends EditTarget {

    private final GameFileContext context;
    private final SavegameStorage<T,I> storage;
    private final SavegameEntry<T, I> entry;
    private final EditTarget target;

    public StorageEditTarget(SavegameStorage<T, I> storage, SavegameEntry<T, I> entry, EditTarget target) {
        super(storage.getSavegameFile(entry));
        this.storage = storage;
        this.entry = entry;
        this.target = target;
        // If savegame failed to load, still allow for editing!
        this.context = entry.getInfo() != null ? GameFileContext.fromData(entry.getInfo()) :
                GameFileContext.forGame(SavegameStorage.ALL.inverseBidiMap().get(storage));
    }

    @Override
    public boolean canSave() {
        if (!storage.contains(entry)) {
            return false;
        }

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
        if (!storage.contains(entry)) {
            GuiErrorReporter.showSimpleErrorMessage("Could not save savegame as it does no longer exist in the storage");
            return;
        }

        target.write(nodeMap);
        SavegameActions.reloadSavegame(entry);
    }

    @Override
    public TextFormatParser getParser() {
        return target.getParser();
    }

    @Override
    public String getName() {
        return storage.getEntryName(entry) + (entry.getInfo() != null && entry.getInfo().getData().isBinary() ? " (Binary/Read-only)" : "");
    }

    @Override
    public GameFileContext getFileContext() {
        return context;
    }
}

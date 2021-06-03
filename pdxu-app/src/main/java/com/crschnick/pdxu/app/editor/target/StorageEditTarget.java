package com.crschnick.pdxu.app.editor.target;

import com.crschnick.pdxu.app.savegame.SavegameActions;
import com.crschnick.pdxu.app.savegame.SavegameEntry;
import com.crschnick.pdxu.app.savegame.SavegameStorage;
import com.crschnick.pdxu.io.parser.TextFormatParser;
import com.crschnick.pdxu.io.savegame.SavegameContent;
import com.crschnick.pdxu.model.SavegameInfo;

public class StorageEditTarget<T, I extends SavegameInfo<T>> extends EditTarget {

    private final SavegameEntry<T, I> entry;
    private final EditTarget target;

    public StorageEditTarget(SavegameStorage<T, I> storage, SavegameEntry<T, I> entry, EditTarget target) {
        super(storage.getSavegameFile(entry));
        this.entry = entry;
        this.target = target;
    }

    @Override
    public SavegameContent parse() throws Exception {
        return target.parse();
    }

    @Override
    public void write(SavegameContent nodeMap) throws Exception {
        target.write(nodeMap);
        SavegameActions.reloadSavegame(entry);
    }

    @Override
    public TextFormatParser getParser() {
        return target.getParser();
    }
}

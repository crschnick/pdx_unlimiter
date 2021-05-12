package com.crschnick.pdx_unlimiter.app.editor.target;

import com.crschnick.pdx_unlimiter.app.savegame.SavegameActions;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameEntry;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameStorage;
import com.crschnick.pdxu.io.node.ArrayNode;
import com.crschnick.pdxu.model.SavegameInfo;

import java.nio.charset.Charset;
import java.util.Map;

public class StorageEditTarget<T, I extends SavegameInfo<T>> extends EditTarget {

    private final SavegameEntry<T, I> entry;
    private final EditTarget target;

    public StorageEditTarget(SavegameStorage<T, I> storage, SavegameEntry<T, I> entry, EditTarget target) {
        super(storage.getSavegameFile(entry));
        this.entry = entry;
        this.target = target;
    }

    @Override
    public Map<String, ArrayNode> parse() throws Exception {
        return target.parse();
    }

    @Override
    public void write(Map<String, ArrayNode> nodeMap) throws Exception {
        target.write(nodeMap);
        SavegameActions.reloadSavegame(entry);
    }

    @Override
    public Charset getCharset() {
        return target.getCharset();
    }
}

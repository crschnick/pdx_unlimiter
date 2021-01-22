package com.crschnick.pdx_unlimiter.app.editor;

import com.crschnick.pdx_unlimiter.app.savegame.SavegameCache;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameEntry;
import com.crschnick.pdx_unlimiter.core.parser.Node;
import com.crschnick.pdx_unlimiter.core.savegame.SavegameInfo;

import java.util.Map;

public class StorageEditTarget<T, I extends SavegameInfo<T>> extends EditTarget {

    private SavegameCache<T, I> storage;
    private SavegameEntry<T, I> entry;
    private EditTarget target;

    public StorageEditTarget(SavegameCache<T, I> storage, SavegameEntry<T, I> entry, EditTarget target) {
        super(storage.getSavegameFile(entry), target.getParser(), target.getWriter());
        this.storage = storage;
        this.entry = entry;
        this.target = target;
    }

    @Override
    public Map<String, Node> parse() throws Exception {
        return target.parse();
    }

    @Override
    public void write(Map<String, Node> nodeMap) throws Exception {
        target.write(nodeMap);
        storage.reloadSavegameAsync(entry);
    }
}

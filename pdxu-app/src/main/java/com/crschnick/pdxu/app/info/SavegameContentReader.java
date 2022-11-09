package com.crschnick.pdxu.app.info;

import com.crschnick.pdxu.io.savegame.SavegameContent;

public abstract class SavegameContentReader {

    public boolean requiresPlayer() {
        return true;
    }

    protected abstract void init(SavegameContent content, SavegameData<?> data);
}

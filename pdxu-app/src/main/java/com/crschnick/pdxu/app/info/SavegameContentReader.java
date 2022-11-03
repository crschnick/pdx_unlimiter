package com.crschnick.pdxu.app.info;

import com.crschnick.pdxu.io.savegame.SavegameContent;

public abstract class SavegameContentReader {

    protected abstract void init(SavegameContent content, SavegameData<?> data);
}

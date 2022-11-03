package com.crschnick.pdxu.app.info;

import com.crschnick.pdxu.io.savegame.SavegameContent;

import java.util.List;

public abstract class SavegameInfoMultiComp extends SavegameContentReader {

    protected abstract void init(SavegameContent content, SavegameData<?> data);

    protected abstract List<? extends SavegameInfoComp> create(SavegameData<?> data);
}

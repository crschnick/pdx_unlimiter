package com.crschnick.pdxu.app.info;

import com.crschnick.pdxu.io.savegame.SavegameContent;
import javafx.scene.layout.Region;

public abstract class SavegameInfoComp {

    protected abstract void init(SavegameContent content, SavegameData<?> data);

    public abstract Region create(SavegameData<?> data);
}

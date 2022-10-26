package com.crschnick.pdxu.app.info.hoi4;

import com.crschnick.pdxu.app.info.*;
import com.crschnick.pdxu.io.savegame.SavegameContent;
import com.crschnick.pdxu.model.hoi4.Hoi4Tag;

public class Hoi4SavegameInfo extends SavegameInfo<Hoi4Tag> {

    ModComp mods;
    VersionComp version;

    public Hoi4SavegameInfo() {
    }

    public Hoi4SavegameInfo(SavegameContent content) throws SavegameInfoException {
        super(content);
    }

    @Override
    protected String getStyleClass() {
        return "hoi4";
    }

    @Override
    protected Class<? extends SavegameData<Hoi4Tag>> getDataClass() {
        return Hoi4SavegameData.class;
    }
}

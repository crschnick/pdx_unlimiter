package com.crschnick.pdxu.app.info.hoi4;

import com.crschnick.pdxu.app.info.ModComp;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.app.info.VersionComp;
import com.crschnick.pdxu.io.savegame.SavegameContent;
import com.crschnick.pdxu.model.hoi4.Hoi4Tag;

public class Hoi4SavegameInfo extends SavegameInfo<Hoi4Tag> {

    ModComp mods;
    VersionComp version;

    public Hoi4SavegameInfo() {
    }

    public Hoi4SavegameInfo(SavegameContent content) throws Exception {
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

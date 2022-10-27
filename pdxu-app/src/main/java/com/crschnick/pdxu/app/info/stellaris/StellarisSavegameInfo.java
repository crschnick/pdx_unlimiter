package com.crschnick.pdxu.app.info.stellaris;

import com.crschnick.pdxu.app.info.*;
import com.crschnick.pdxu.io.savegame.SavegameContent;
import com.crschnick.pdxu.model.stellaris.StellarisTag;

public class StellarisSavegameInfo extends SavegameInfo<StellarisTag> {

    DlcComp dlcs;
    VersionComp version;

    public StellarisSavegameInfo() {
    }

    public StellarisSavegameInfo(SavegameContent content) throws Exception {
        super(content);
    }

    @Override
    protected String getStyleClass() {
        return "stellaris";
    }

    @Override
    protected Class<? extends SavegameData<StellarisTag>> getDataClass() {
        return StellarisSavegameData.class;
    }
}

package com.crschnick.pdxu.app.info.stellaris;

import com.crschnick.pdxu.app.info.DlcComp;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.app.info.SavegameInfoException;
import com.crschnick.pdxu.io.savegame.SavegameContent;
import com.crschnick.pdxu.model.stellaris.StellarisTag;

public class StellarisSavegameInfo extends SavegameInfo<StellarisTag> {

    DlcComp dlcs;

    public StellarisSavegameInfo() {
    }

    public StellarisSavegameInfo(SavegameContent content) throws SavegameInfoException {
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

package com.crschnick.pdxu.app.info.ck2;

import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.app.info.SavegameInfoException;
import com.crschnick.pdxu.io.savegame.SavegameContent;
import com.crschnick.pdxu.model.ck2.Ck2Tag;

public class Ck2SavegameInfo extends SavegameInfo<Ck2Tag> {

    public Ck2SavegameInfo(SavegameContent content) throws SavegameInfoException {
        super(content);
    }

    @Override
    protected String getStyleClass() {
        return "ck2";
    }

    @Override
    protected Class<? extends SavegameData<Ck2Tag>> getDataClass() {
        return null;
    }

}

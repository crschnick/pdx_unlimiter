package com.crschnick.pdxu.app.info.vic3;

import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.app.info.SavegameInfoException;
import com.crschnick.pdxu.io.savegame.SavegameContent;
import com.crschnick.pdxu.model.vic3.Vic3Tag;

public class Vic3SavegameInfo extends SavegameInfo<Vic3Tag> {

    public Vic3SavegameInfo() {
    }


    public Vic3SavegameInfo(SavegameContent content) throws SavegameInfoException {
        super(content);
    }

    @Override
    protected String getStyleClass() {
        return "vic3";
    }

    @Override
    protected Class<? extends SavegameData<Vic3Tag>> getDataClass() {
        return Vic3SavegameData.class;
    }

}

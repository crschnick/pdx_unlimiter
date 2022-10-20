package com.crschnick.pdxu.app.info.vic2;

import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.app.info.SavegameInfoException;
import com.crschnick.pdxu.io.savegame.SavegameContent;
import com.crschnick.pdxu.model.vic2.Vic2Tag;

public class Vic2SavegameInfo extends SavegameInfo<Vic2Tag> {


    public Vic2SavegameInfo(SavegameContent content) throws SavegameInfoException {
        super(content);
    }

    @Override
    protected String getStyleClass() {
        return "vic2";
    }

    @Override
    protected Class<? extends SavegameData<Vic2Tag>> getDataClass() {
        return null;
    }
}

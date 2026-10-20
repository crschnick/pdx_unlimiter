package com.crschnick.pdxu.app.info.ck2;

import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.app.info.VersionComp;
import com.crschnick.pdxu.io.savegame.SavegameContent;
import com.crschnick.pdxu.model.ck2.Ck2Tag;

public class Ck2SavegameInfo extends SavegameInfo<Ck2Tag> {

    Ck2RulerComp ruler;
    Ck2GoldComp gold;
    Ck2PrestigeComp prestige;
    Ck2PietyComp piety;
    Ck2IronmanComp ironman;
    VersionComp version;

    public Ck2SavegameInfo() {}

    public Ck2SavegameInfo(SavegameContent content) throws Exception {
        super(content);
    }

    @Override
    protected String getStyleClass() {
        return "ck2";
    }

    @Override
    protected Class<? extends SavegameData<Ck2Tag>> getDataClass() {
        return Ck2SavegameData.class;
    }
}

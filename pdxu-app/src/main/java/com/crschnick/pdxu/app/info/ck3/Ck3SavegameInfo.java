package com.crschnick.pdxu.app.info.ck3;

import com.crschnick.pdxu.app.info.*;
import com.crschnick.pdxu.io.savegame.SavegameContent;
import com.crschnick.pdxu.model.ck3.Ck3Tag;

public class Ck3SavegameInfo extends SavegameInfo<Ck3Tag> {

    Ck3RulerComp ruler;
    Ck3GoldComp gold;
    Ck3PrestigeComp prestige;
    Ck3PietyComp piety;
    Ck3SoldiersComp soldiers;
    Ck3TitlesComp titles;
    Ck3ClaimsComp claims;
    Ck3WarMultiComp wars;
    Ck3AlliesComp allies;
    Ck3IronmanComp ironman;
    ModComp mods;
    DlcComp dlcs;
    VersionComp version;

    public Ck3SavegameInfo() {
    }

    public Ck3SavegameInfo(SavegameContent content) throws Exception {
        super(content);
    }

    @Override
    protected String getStyleClass() {
        return "ck3";
    }

    @Override
    protected Class<? extends SavegameData<Ck3Tag>> getDataClass() {
        return Ck3SavegameData.class;
    }

}

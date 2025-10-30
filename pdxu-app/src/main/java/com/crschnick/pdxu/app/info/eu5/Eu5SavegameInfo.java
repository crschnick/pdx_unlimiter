package com.crschnick.pdxu.app.info.eu5;

import com.crschnick.pdxu.app.info.ModComp;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.app.info.VersionComp;
import com.crschnick.pdxu.io.savegame.SavegameContent;
import com.crschnick.pdxu.model.eu5.Eu5Tag;
import com.crschnick.pdxu.model.hoi4.Hoi4Tag;

public class Eu5SavegameInfo extends SavegameInfo<Eu5Tag> {

    Eu5RulerComp ruler;
    Eu5HeirComp heir;
    Eu5DucatsComp ducats;
    Eu5StabilityComp stability;
    Eu5PopulationComp population;
    Eu5LegitimacyComp legitimacy;
    Eu5PrestigeComp prestige;
    Eu5ManpowerComp manpower;
    Eu5WarMultiComp wars;
    ModComp mods;
    VersionComp version;

    public Eu5SavegameInfo() {}

    public Eu5SavegameInfo(SavegameContent content) throws Exception {
        super(content);
    }

    @Override
    protected String getStyleClass() {
        return "eu5";
    }

    @Override
    protected Class<? extends SavegameData<Eu5Tag>> getDataClass() {
        return Eu5SavegameData.class;
    }
}

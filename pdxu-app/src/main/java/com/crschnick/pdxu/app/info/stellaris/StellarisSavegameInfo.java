package com.crschnick.pdxu.app.info.stellaris;

import com.crschnick.pdxu.app.info.DlcComp;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.app.info.VersionComp;
import com.crschnick.pdxu.io.savegame.SavegameContent;
import com.crschnick.pdxu.model.stellaris.StellarisTag;

public class StellarisSavegameInfo extends SavegameInfo<StellarisTag> {

    StellarisPlanetsComp planets;
    StellarisFleetsComp fleets;
    StellarisEnergyComp energy;
    StellarisMineralsComp minerals;
    StellarisFoodComp food;
    StellarisAlloysComp alloys;
    StellarisAlliesComp allies;
    StellarisWarMultiComp wars;
    StellarisIronmanComp ironman;
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

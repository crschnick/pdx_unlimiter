package com.crschnick.pdxu.app.info.stellaris;

import com.crschnick.pdxu.app.info.DlcComp;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.app.info.VersionComp;
import com.crschnick.pdxu.io.savegame.SavegameContent;
import com.crschnick.pdxu.model.stellaris.StellarisTag;

@SuppressWarnings("unused")
public class StellarisSavegameInfo extends SavegameInfo<StellarisTag> {

    StellarisEmpireSizeComp empireSize;
    StellarisPlanetsComp planets;
    StellarisFleetsComp fleets;
    StellarisEnergyComp energy;
    StellarisMineralsComp minerals;
    StellarisFoodComp food;
    StellarisConsumerGoodsComp consumerGoods;
    StellarisAlloysComp alloys;
    StellarisInfluenceComp influence;
    StellarisUnityComp unity;
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

package com.crschnick.pdxu.app.info.stellaris;

import com.crschnick.pdxu.app.info.DlcComp;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.app.info.VersionComp;
import com.crschnick.pdxu.io.savegame.SavegameContent;
import com.crschnick.pdxu.model.stellaris.StellarisTag;

@SuppressWarnings("unused")
public class StellarisSavegameInfo extends SavegameInfo<StellarisTag> {

    StellarisVictoryRankComp victoryRank;
    StellarisPowerComp power;
    StellarisEmpireSizeComp empireSize;
    StellarisPopsComp pops;
    StellarisPlanetsComp planets;
    StellarisFleetsComp fleets;
    StellarisStarbaseCapacityComp starbaseCapacity;
    StellarisEnergyComp energy;
    StellarisMineralsComp minerals;
    StellarisAlloysComp alloys;
    StellarisFoodComp food;
    StellarisConsumerGoodsComp consumerGoods;
    StellarisInfluenceComp influence;
    StellarisUnityComp unity;
    StellarisTradeComp trade;
    StellarisPhysicsResearchComp physicsResearch;
    StellarisSocietyResearchComp societyResearch;
    StellarisEngineeringResearchComp engineeringResearch;
    StellarisAlliesComp allies;
    StellarisWarMultiComp wars;
    StellarisIronmanComp ironman;
    DlcComp dlcs;
    VersionComp version;

    public StellarisSavegameInfo() {}

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

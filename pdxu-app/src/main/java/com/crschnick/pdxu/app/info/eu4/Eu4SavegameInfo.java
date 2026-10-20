package com.crschnick.pdxu.app.info.eu4;

import com.crschnick.pdxu.app.info.*;
import com.crschnick.pdxu.io.savegame.SavegameContent;
import com.crschnick.pdxu.model.eu4.Eu4Tag;

@SuppressWarnings("unused")
public class Eu4SavegameInfo extends SavegameInfo<Eu4Tag> {

    Eu4RulerComp ruler;
    Eu4HeirComp heir;
    Eu4DucatsComp ducats;
    Eu4InflationComp inflation;
    Eu4ManpowerComp manpower;
    Eu4DevelopmentComp development;
    Eu4PowersComp powers;
    Eu4StabilityComp stability;
    Eu4LegitimacyComp legitimacy;
    Eu4PrestigeComp prestige;
    Eu4AlliesComp allies;
    Eu4VassalsComp vassals;
    Eu4PersonalUnionsComp personalUnions;
    Eu4WarMultiComp wars;
    Eu4AchievementOkComp achievements;
    Eu4IronmanComp ironman;
    Eu4RnwComp randomNewWorld;
    Eu4CustomNationComp customNation;
    Eu4ReleasedVassalComp releasedVassal;
    ModComp mods;
    DlcComp dlcs;
    VersionComp version;

    public Eu4SavegameInfo() {}

    public Eu4SavegameInfo(SavegameContent content) throws Exception {
        super(content);
    }

    @Override
    protected String getStyleClass() {
        return "eu4";
    }

    @Override
    protected Class<? extends SavegameData<Eu4Tag>> getDataClass() {
        return Eu4SavegameData.class;
    }
}

package com.crschnick.pdxu.app.info.eu4;

import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.io.node.ArrayNode;
import com.crschnick.pdxu.model.eu4.Eu4Tag;

public class Eu4SavegameInfo extends SavegameInfo<Eu4Tag> {

    private Eu4RulerComp ruler;
    private Eu4HeirComp heir;
    private Eu4IronmanComp ironman;
    private Eu4AchievementOkComp achievements;
    private Eu4RnwComp randomNewWorld;
    private Eu4CustomNationComp customNation;
    private Eu4ReleasedVassalComp releasedVassal;
    private Eu4DucatsComp ducats;
    private Eu4ManpowerComp manpower;
    private Eu4StabilityComp stability;
    private Eu4PrestigeComp prestige;
    private Eu4PowersComp powers;
    private Eu4DevelopmentComp development;
    Eu4AlliesComp allies;
    Eu4WarMultiComp wars;

    public Eu4SavegameInfo() {}

    public Eu4SavegameInfo(ArrayNode node) throws Exception {
        super(node);
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

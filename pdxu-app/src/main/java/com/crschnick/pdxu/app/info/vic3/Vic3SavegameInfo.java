package com.crschnick.pdxu.app.info.vic3;

import com.crschnick.pdxu.app.info.*;
import com.crschnick.pdxu.io.savegame.SavegameContent;
import com.crschnick.pdxu.model.vic3.Vic3Tag;

@SuppressWarnings("unused")
public class Vic3SavegameInfo extends SavegameInfo<Vic3Tag> {

    Vic3AchievementOkComp achievementOk;
    Vic3IronmanComp ironman;
    Vic3GdpComp gdp;
    Vic3SolComp sol;
    Vic3PrestigeComp prestige;
    Vic3LoyalistComp loyalist;
    Vic3RadicalComp radical;
    ModComp mods;
    DlcComp dlcs;
    VersionComp version;

    public Vic3SavegameInfo() {
    }


    public Vic3SavegameInfo(SavegameContent content) throws Exception {
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

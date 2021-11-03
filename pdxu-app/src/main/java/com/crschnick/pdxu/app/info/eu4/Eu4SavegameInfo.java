package com.crschnick.pdxu.app.info.eu4;

import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.io.node.ArrayNode;

public class Eu4SavegameInfo extends SavegameInfo<Eu4SavegameData> {

    private Eu4RulerComp ruler;
    private Eu4HeirComp heir;
    private Eu4IronmanComp ironman;
    private Eu4AchievementComp achievements;

    protected Eu4SavegameInfo(ArrayNode node) throws Exception {
        super(node);
    }

    @Override
    protected Class<Eu4SavegameData> getDataClass() {
        return Eu4SavegameData.class;
    }
}

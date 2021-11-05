package com.crschnick.pdxu.app.info.eu4;

import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.io.node.ArrayNode;
import com.crschnick.pdxu.model.eu4.Eu4Tag;

public class Eu4SavegameInfo extends SavegameInfo<Eu4Tag> {

    private Eu4RulerComp ruler;
    private Eu4HeirComp heir;
    private Eu4IronmanComp ironman;
    private Eu4AchievementComp achievements;

    public Eu4SavegameInfo() {}

    public Eu4SavegameInfo(ArrayNode node) throws Exception {
        super(node);
    }

    @Override
    protected Class<? extends SavegameData<Eu4Tag>> getDataClass() {
        return Eu4SavegameData.class;
    }

}

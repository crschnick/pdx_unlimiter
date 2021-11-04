package com.crschnick.pdxu.app.info.hoi4;

import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.io.node.ArrayNode;
import com.crschnick.pdxu.model.hoi4.Hoi4Tag;

public class Hoi4SavegameInfo extends SavegameInfo<Hoi4Tag> {

    protected Hoi4SavegameInfo(ArrayNode node) throws Exception {
        super(node);
    }

    @Override
    protected Class<? extends SavegameData<Hoi4Tag>> getDataClass() {
        return null;
    }
}

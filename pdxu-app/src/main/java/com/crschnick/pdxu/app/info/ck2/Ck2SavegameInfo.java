package com.crschnick.pdxu.app.info.ck2;

import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.io.node.ArrayNode;
import com.crschnick.pdxu.model.ck2.Ck2Tag;

public class Ck2SavegameInfo extends SavegameInfo<Ck2Tag> {

    protected Ck2SavegameInfo(ArrayNode node) throws Exception {
        super(node);
    }

    @Override
    protected String getStyleClass() {
        return "ck2";
    }

    @Override
    protected Class<? extends SavegameData<Ck2Tag>> getDataClass() {
        return null;
    }

}

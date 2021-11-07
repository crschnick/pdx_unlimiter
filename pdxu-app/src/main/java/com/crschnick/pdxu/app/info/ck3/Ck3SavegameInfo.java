package com.crschnick.pdxu.app.info.ck3;

import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.io.node.ArrayNode;
import com.crschnick.pdxu.model.ck3.Ck3Tag;

public class Ck3SavegameInfo extends SavegameInfo<Ck3Tag> {

    protected Ck3SavegameInfo(ArrayNode node) throws Exception {
        super(node);
    }

    @Override
    protected String getStyleClass() {
        return "ck3";
    }

    @Override
    protected Class<? extends SavegameData<Ck3Tag>> getDataClass() {
        return null;
    }

}

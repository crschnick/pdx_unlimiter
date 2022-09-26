package com.crschnick.pdxu.app.info.vic3;

import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.io.node.ArrayNode;
import com.crschnick.pdxu.model.vic3.Vic3Tag;

public class Vic3SavegameInfo extends SavegameInfo<Vic3Tag> {

    protected Vic3SavegameInfo(ArrayNode node) throws Exception {
        super(node);
    }

    @Override
    protected String getStyleClass() {
        return "vic3";
    }

    @Override
    protected Class<? extends SavegameData<Vic3Tag>> getDataClass() {
        return null;
    }
}

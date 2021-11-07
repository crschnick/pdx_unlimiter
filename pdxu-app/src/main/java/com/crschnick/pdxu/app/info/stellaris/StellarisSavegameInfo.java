package com.crschnick.pdxu.app.info.stellaris;

import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.io.node.ArrayNode;
import com.crschnick.pdxu.model.stellaris.StellarisTag;

public class StellarisSavegameInfo extends SavegameInfo<StellarisTag> {

    protected StellarisSavegameInfo(ArrayNode node) throws Exception {
        super(node);
    }

    @Override
    protected String getStyleClass() {
        return "stellaris";
    }

    @Override
    protected Class<? extends SavegameData<StellarisTag>> getDataClass() {
        return null;
    }
}

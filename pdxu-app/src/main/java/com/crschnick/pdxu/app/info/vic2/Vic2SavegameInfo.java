package com.crschnick.pdxu.app.info.vic2;

import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.io.node.ArrayNode;
import com.crschnick.pdxu.model.vic2.Vic2Tag;

public class Vic2SavegameInfo extends SavegameInfo<Vic2Tag> {

    protected Vic2SavegameInfo(ArrayNode node) throws Exception {
        super(node);
    }

    @Override
    protected Class<? extends SavegameData<Vic2Tag>> getDataClass() {
        return null;
    }
}

package com.crschnick.pdxu.app.info.vic2;

import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SimpleInfoComp;
import com.crschnick.pdxu.app.util.NumberFormatHelper;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.savegame.SavegameContent;

public abstract class Vic2CountryValueComp extends SimpleInfoComp {

    protected double value;

    protected abstract String getKey();

    @Override
    protected void init(SavegameContent content, SavegameData<?> data) {
        value = data.vic2()
                .getPlayerCountry(content)
                .flatMap(c -> c.getNodeForKeyIfExistent(getKey()))
                .filter(Node::isValue)
                .map(Node::getDouble)
                .orElse(0.0);
    }

    @Override
    protected String getDisplayValue() {
        return NumberFormatHelper.formatCompact(value);
    }
}

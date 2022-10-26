package com.crschnick.pdxu.app.info;

import com.crschnick.pdxu.app.lang.PdxuI18n;
import com.crschnick.pdxu.io.savegame.SavegameContent;

public abstract class IronmanComp extends SimpleInfoComp {

    private boolean ironman;

    @Override
    protected String getTooltip() {
        return PdxuI18n.get("IRONMAN");
    }

    @Override
    protected String getDisplayValue() {
        return null;
    }

    @Override
    protected void init(SavegameContent content, SavegameData<?> data) {
        this.ironman = data.isIronman();
    }

    @Override
    protected boolean shouldShow() {
        return ironman;
    }
}

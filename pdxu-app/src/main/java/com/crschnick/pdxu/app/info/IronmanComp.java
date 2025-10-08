package com.crschnick.pdxu.app.info;

import com.crschnick.pdxu.app.core.AppI18n;

import com.crschnick.pdxu.io.savegame.SavegameContent;

public abstract class IronmanComp extends SimpleInfoComp {

    private boolean ironman;

    @Override
    public boolean requiresPlayer() {
        return false;
    }

    @Override
    protected String getTooltip() {
        return AppI18n.get("ironman");
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

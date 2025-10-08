package com.crschnick.pdxu.app.info.eu4;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SimpleInfoComp;

import com.crschnick.pdxu.io.savegame.SavegameContent;
import javafx.scene.image.Image;

public class Eu4IronmanComp extends SimpleInfoComp {

    private boolean ironman;

    @Override
    public boolean requiresPlayer() {
        return false;
    }

    @Override
    protected Image getImage() {
        return GameImage.EU4_ICON_IRONMAN;
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

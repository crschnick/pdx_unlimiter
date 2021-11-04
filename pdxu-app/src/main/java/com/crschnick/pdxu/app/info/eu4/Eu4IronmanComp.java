package com.crschnick.pdxu.app.info.eu4;

import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SimpleInfoComp;
import com.crschnick.pdxu.app.lang.PdxuI18n;
import com.crschnick.pdxu.io.node.ArrayNode;
import javafx.scene.image.Image;

public class Eu4IronmanComp extends SimpleInfoComp {

    private boolean ironman;

    @Override
    protected Image getImage() {
        return GameImage.EU4_ICON_IRONMAN;
    }

    @Override
    protected String getTooltip() {
        return PdxuI18n.get("IRONMAN");
    }

    @Override
    protected String getDisplayValue() {
        return null;
    }

    @Override
    protected void init(ArrayNode node, SavegameData data) {
        this.ironman = data.isIronman();
    }

    @Override
    protected boolean shouldShow() {
        return ironman;
    }
}

package com.crschnick.pdxu.app.info;

import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.lang.PdxuI18n;
import javafx.scene.image.Image;

public class Eu4IronmanComp extends SimpleInfoComp {

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
}

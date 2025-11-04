package com.crschnick.pdxu.app.info.eu5;

import com.crschnick.pdxu.app.gui.game.GameImage;

import javafx.scene.image.Image;

public class Eu5HeirComp extends Eu5RulerComp {

    @Override
    protected String getKey() {
        return "heir";
    }

    @Override
    protected Image getIcon() {
        return GameImage.EU5_ICON_HEIR;
    }

    @Override
    protected Ruler getDefault() {
        return null;
    }
}

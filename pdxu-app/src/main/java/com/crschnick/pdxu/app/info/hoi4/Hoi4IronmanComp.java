package com.crschnick.pdxu.app.info.hoi4;

import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.IronmanComp;

import javafx.scene.image.Image;

public class Hoi4IronmanComp extends IronmanComp {
    @Override
    protected Image getImage() {
        return GameImage.HOI4_ICON_IRONMAN;
    }
}

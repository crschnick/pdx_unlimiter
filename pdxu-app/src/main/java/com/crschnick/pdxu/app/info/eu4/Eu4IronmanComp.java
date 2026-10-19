package com.crschnick.pdxu.app.info.eu4;

import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.IronmanComp;

import javafx.scene.image.Image;

public class Eu4IronmanComp extends IronmanComp {
    @Override
    protected Image getImage() {
        return GameImage.EU4_ICON_IRONMAN;
    }
}

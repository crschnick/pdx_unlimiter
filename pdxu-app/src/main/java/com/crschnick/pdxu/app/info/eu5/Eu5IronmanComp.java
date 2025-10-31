package com.crschnick.pdxu.app.info.eu5;

import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.IronmanComp;
import javafx.scene.image.Image;

public class Eu5IronmanComp extends IronmanComp {
    @Override
    protected Image getImage() {
        return GameImage.EU5_ICON_IRONMAN;
    }
}

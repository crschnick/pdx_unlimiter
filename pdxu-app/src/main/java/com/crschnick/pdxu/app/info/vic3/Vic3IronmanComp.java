package com.crschnick.pdxu.app.info.vic3;

import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.IronmanComp;
import javafx.scene.image.Image;

public class Vic3IronmanComp extends IronmanComp {
    @Override
    protected Image getImage() {
        return GameImage.VIC3_ICON_IRONMAN;
    }
}

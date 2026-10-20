package com.crschnick.pdxu.app.info.ck2;

import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.IronmanComp;

import javafx.scene.image.Image;

public class Ck2IronmanComp extends IronmanComp {

    @Override
    protected Image getImage() {
        return GameImage.CK2_ICON_IRONMAN;
    }
}

package com.crschnick.pdxu.app.info.stellaris;

import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.IronmanComp;

import javafx.scene.image.Image;

public class StellarisIronmanComp extends IronmanComp {
    @Override
    protected Image getImage() {
        return GameImage.STELLARIS_ICON_IRONMAN;
    }
}

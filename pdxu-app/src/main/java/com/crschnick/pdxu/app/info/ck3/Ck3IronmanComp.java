package com.crschnick.pdxu.app.info.ck3;

import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.IronmanComp;
import javafx.scene.image.Image;

public class Ck3IronmanComp extends IronmanComp {
    @Override
    protected Image getImage() {
        return GameImage.CK3_ICON_IRONMAN;
    }
}

package com.crschnick.pdxu.app.info.stellaris;

import com.crschnick.pdxu.app.gui.game.GameImage;
import javafx.scene.image.Image;

public class StellarisAlloysComp extends StellarisResourceComp {

    @Override
    protected Image getImage() {
        return GameImage.STELLARIS_ICON_ALLOYS;
    }

    @Override
    protected String getResourceName() {
        return "alloys";
    }
}

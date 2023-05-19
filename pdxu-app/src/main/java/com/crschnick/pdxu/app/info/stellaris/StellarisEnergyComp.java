package com.crschnick.pdxu.app.info.stellaris;

import com.crschnick.pdxu.app.gui.game.GameImage;
import javafx.scene.image.Image;

public class StellarisEnergyComp extends StellarisResourceComp {

    @Override
    protected Image getImage() {
        return GameImage.STELLARIS_ICON_ENERGY;
    }

    @Override
    protected String getResourceName() {
        return "energy";
    }
}

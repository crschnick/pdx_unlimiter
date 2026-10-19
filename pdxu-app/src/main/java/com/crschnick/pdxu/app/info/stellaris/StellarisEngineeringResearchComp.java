package com.crschnick.pdxu.app.info.stellaris;

import com.crschnick.pdxu.app.gui.game.GameImage;

import javafx.scene.image.Image;

public class StellarisEngineeringResearchComp extends StellarisResourceComp {

    @Override
    protected Image getImage() {
        return GameImage.STELLARIS_ICON_ENGINEERING_RESEARCH;
    }

    @Override
    protected String getResourceName() {
        return "engineering_research";
    }
}

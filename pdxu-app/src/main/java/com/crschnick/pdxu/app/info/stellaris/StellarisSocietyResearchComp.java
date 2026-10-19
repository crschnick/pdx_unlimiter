package com.crschnick.pdxu.app.info.stellaris;

import com.crschnick.pdxu.app.gui.game.GameImage;

import javafx.scene.image.Image;

public class StellarisSocietyResearchComp extends StellarisResourceComp {

    @Override
    protected Image getImage() {
        return GameImage.STELLARIS_ICON_SOCIETY_RESEARCH;
    }

    @Override
    protected String getResourceName() {
        return "society_research";
    }
}

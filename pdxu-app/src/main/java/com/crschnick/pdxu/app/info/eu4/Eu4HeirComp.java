package com.crschnick.pdxu.app.info.eu4;

import com.crschnick.pdxu.app.gui.game.GameImage;
import javafx.scene.image.Image;

public class Eu4HeirComp extends Eu4RulerComp {

    @Override
    protected String getRulerKey() {
        return "heir";
    }

    @Override
    protected Image getIcon() {
        return GameImage.EU4_ICON_HEIR;
    }

    @Override
    protected Ruler getDefault() {
        return null;
    }
}

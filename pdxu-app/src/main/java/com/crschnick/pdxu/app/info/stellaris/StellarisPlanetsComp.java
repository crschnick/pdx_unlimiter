package com.crschnick.pdxu.app.info.stellaris;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SimpleInfoComp;

import com.crschnick.pdxu.io.savegame.SavegameContent;
import javafx.scene.image.Image;

public class StellarisPlanetsComp extends SimpleInfoComp {

    private int planets;

    @Override
    protected void init(SavegameContent content, SavegameData<?> data) {
        planets = (int) content.get().getNodeForKeys("meta_planets").getDouble();
    }

    @Override
    protected String getDisplayValue() {
        return String.valueOf(planets);
    }

    @Override
    protected Image getImage() {
        return GameImage.STELLARIS_ICON_PLANETS;
    }

    @Override
    protected String getTooltip() {
        return AppI18n.get("planets");
    }
}

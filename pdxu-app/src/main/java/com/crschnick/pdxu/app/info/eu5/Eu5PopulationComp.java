package com.crschnick.pdxu.app.info.eu5;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SimpleInfoComp;
import com.crschnick.pdxu.io.savegame.SavegameContent;
import javafx.scene.image.Image;

public class Eu5PopulationComp extends SimpleInfoComp {

    private int value;

    @Override
    protected String getDisplayValue() {
        return String.valueOf(value) + "k";
    }

    @Override
    protected void init(SavegameContent content, SavegameData<?> data) {
        value = (int) content.get()
                .getNodeForKey("countries")
                .getNodeForKey("database")
                .getNodeForKey(data.eu5().getTag().getId() + "")
                .getNodeForKey("last_months_population")
                .getDouble();
    }

    @Override
    protected Image getImage() {
        return GameImage.EU5_ICON_POPULATION;
    }

    @Override
    protected String getTooltip() {
        return AppI18n.get("population");
    }
}

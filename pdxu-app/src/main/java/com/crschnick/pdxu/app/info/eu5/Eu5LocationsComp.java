package com.crschnick.pdxu.app.info.eu5;

import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SimpleInfoComp;
import com.crschnick.pdxu.app.installation.GameLocalisation;
import com.crschnick.pdxu.io.savegame.SavegameContent;

import javafx.scene.image.Image;
import javafx.scene.layout.Region;

public class Eu5LocationsComp extends SimpleInfoComp {

    private int value;
    private transient String tooltip;

    @Override
    protected void init(SavegameContent content, SavegameData<?> data) {
        value = content.get()
                .getNodeForKey("countries")
                .getNodeForKey("database")
                .getNodeForKey(data.eu5().getTag().getId() + "")
                .getNodeForKeyIfExistent("owned_locations")
                .map(n -> n.getNodeArray().size())
                .orElse(0);
    }

    @Override
    public Region create(SavegameData<?> data) {
        tooltip = GameLocalisation.getLocalisedValue("game_concept_locations", data);
        return super.create(data);
    }

    @Override
    protected String getDisplayValue() {
        return String.valueOf(value);
    }

    @Override
    protected Image getImage() {
        return GameImage.EU5_ICON_LOCATIONS;
    }

    @Override
    protected String getTooltip() {
        return tooltip;
    }
}

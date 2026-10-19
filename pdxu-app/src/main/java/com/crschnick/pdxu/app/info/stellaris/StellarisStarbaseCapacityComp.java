package com.crschnick.pdxu.app.info.stellaris;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SimpleInfoComp;
import com.crschnick.pdxu.io.savegame.SavegameContent;

import javafx.scene.image.Image;

public class StellarisStarbaseCapacityComp extends SimpleInfoComp {

    private int used;
    private int max;

    @Override
    protected void init(SavegameContent content, SavegameData<?> data) {
        used = (int) data.stellaris().getPlayerValue(content, "starbase_capacity_used");
        max = (int) data.stellaris().getPlayerValue(content, "starbase_capacity");
    }

    @Override
    protected String getDisplayValue() {
        return used + " / " + max;
    }

    @Override
    protected Image getImage() {
        return GameImage.STELLARIS_ICON_STARBASE;
    }

    @Override
    protected String getTooltip() {
        return AppI18n.get("starbaseCapacity");
    }
}

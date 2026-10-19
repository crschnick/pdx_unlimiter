package com.crschnick.pdxu.app.info.stellaris;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SimpleInfoComp;
import com.crschnick.pdxu.app.util.NumberFormatHelper;
import com.crschnick.pdxu.io.savegame.SavegameContent;

import javafx.scene.image.Image;

public class StellarisNavalCapacityComp extends SimpleInfoComp {

    private double used;

    @Override
    protected void init(SavegameContent content, SavegameData<?> data) {
        used = data.stellaris().getPlayerValue(content, "used_naval_capacity");
    }

    @Override
    protected String getDisplayValue() {
        return NumberFormatHelper.formatCompact(used);
    }

    @Override
    protected Image getImage() {
        return GameImage.STELLARIS_ICON_NAVAL_CAPACITY;
    }

    @Override
    protected String getTooltip() {
        return AppI18n.get("usedNavalCapacity");
    }
}

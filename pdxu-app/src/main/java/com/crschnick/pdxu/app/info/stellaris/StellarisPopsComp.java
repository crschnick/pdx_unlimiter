package com.crschnick.pdxu.app.info.stellaris;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SimpleInfoComp;
import com.crschnick.pdxu.app.util.NumberFormatHelper;
import com.crschnick.pdxu.io.savegame.SavegameContent;

import javafx.scene.image.Image;

public class StellarisPopsComp extends SimpleInfoComp {

    private double pops;

    @Override
    protected void init(SavegameContent content, SavegameData<?> data) {
        pops = data.stellaris().getPlayerValue(content, "num_sapient_pops");
    }

    @Override
    protected String getDisplayValue() {
        return NumberFormatHelper.formatCompact(pops);
    }

    @Override
    protected Image getImage() {
        return GameImage.STELLARIS_ICON_POPS;
    }

    @Override
    protected String getTooltip() {
        return AppI18n.get("population");
    }
}

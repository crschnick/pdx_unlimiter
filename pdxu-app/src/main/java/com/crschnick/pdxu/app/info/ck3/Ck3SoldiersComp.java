package com.crschnick.pdxu.app.info.ck3;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SimpleInfoComp;
import com.crschnick.pdxu.app.util.NumberFormatHelper;
import com.crschnick.pdxu.io.savegame.SavegameContent;

import javafx.scene.image.Image;

public class Ck3SoldiersComp extends SimpleInfoComp {

    private int soldiers;

    @Override
    protected void init(SavegameContent content, SavegameData<?> data) {
        soldiers = data.ck3().getTag().getStrength();
    }

    @Override
    protected String getDisplayValue() {
        return NumberFormatHelper.formatCompact(soldiers);
    }

    @Override
    protected Image getImage() {
        return GameImage.CK3_ICON_SOLDIERS;
    }

    @Override
    protected String getTooltip() {
        return AppI18n.get("totalSoldiers");
    }
}

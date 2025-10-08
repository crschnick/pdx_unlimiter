package com.crschnick.pdxu.app.info.ck3;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SimpleInfoComp;

import com.crschnick.pdxu.io.savegame.SavegameContent;
import javafx.scene.image.Image;

import static com.crschnick.pdxu.app.gui.game.GameImage.CK3_ICON_SOLDIERS;

public class Ck3SoldiersComp extends SimpleInfoComp {

    private int soldiers;

    @Override
    protected void init(SavegameContent content, SavegameData<?> data) {
        soldiers = data.ck3().getTag().getStrength();
    }

    @Override
    protected String getDisplayValue() {
        return String.valueOf(soldiers);
    }

    @Override
    protected Image getImage() {
        return CK3_ICON_SOLDIERS;
    }

    @Override
    protected String getTooltip() {
        return AppI18n.get("totalSoldiers");
    }
}

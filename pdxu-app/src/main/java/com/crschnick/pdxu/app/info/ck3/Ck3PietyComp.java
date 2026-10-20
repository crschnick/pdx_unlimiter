package com.crschnick.pdxu.app.info.ck3;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SimpleInfoComp;
import com.crschnick.pdxu.io.savegame.SavegameContent;

import javafx.scene.image.Image;

public class Ck3PietyComp extends SimpleInfoComp {

    private int value;

    @Override
    protected void init(SavegameContent content, SavegameData<?> data) {
        value = data.ck3().getTag().getPiety();
    }

    @Override
    protected String getDisplayValue() {
        return String.valueOf(value);
    }

    @Override
    protected Image getImage() {
        return GameImage.CK3_ICON_PIETY;
    }

    @Override
    protected String getTooltip() {
        return AppI18n.get("piety");
    }
}

package com.crschnick.pdxu.app.info.eu5;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SimpleInfoComp;
import com.crschnick.pdxu.io.savegame.SavegameContent;

import javafx.scene.image.Image;

public class Eu5ManpowerComp extends SimpleInfoComp {

    private int value;
    private int max;

    @Override
    protected void init(SavegameContent content, SavegameData<?> data) {
        var cd = content.get()
                .getNodeForKey("countries")
                .getNodeForKey("database")
                .getNodeForKey(data.eu5().getTag().getId() + "")
                .getNodeForKey("currency_data");
        value = cd.hasKey("manpower") ? (int) (cd.getNodeForKey("manpower").getDouble() * 1000.0) : 0;

        var tag = content.get()
                .getNodeForKey("countries")
                .getNodeForKey("database")
                .getNodeForKey(data.eu5().getTag().getId() + "");
        max = tag.hasKey("max_manpower")
                ? (int) (tag.getNodeForKey("max_manpower").getDouble() * 1000.0)
                : 0;
    }

    @Override
    protected String getDisplayValue() {
        return value + " / " + max;
    }

    @Override
    protected Image getImage() {
        return GameImage.EU5_ICON_MANPOWER;
    }

    @Override
    protected String getTooltip() {
        return AppI18n.get("manpower") + " / " + AppI18n.get("maxManpower");
    }
}

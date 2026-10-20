package com.crschnick.pdxu.app.info.eu5;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SimpleInfoComp;
import com.crschnick.pdxu.app.util.NumberFormatHelper;
import com.crschnick.pdxu.io.savegame.SavegameContent;

import javafx.scene.image.Image;

public class Eu5ManpowerComp extends SimpleInfoComp {

    private int value;
    private int max;

    @Override
    protected void init(SavegameContent content, SavegameData<?> data) {
        var country = content.get()
                .getNodeForKey("countries")
                .getNodeForKey("database")
                .getNodeForKey(data.eu5().getTag().getId() + "");
        var cd = country.getNodeForKey("currency_data");
        value = cd.hasKey("manpower") ? (int) (cd.getNodeForKey("manpower").getDouble() * 1000.0) : 0;
        max = country.hasKey("max_manpower")
                ? (int) (country.getNodeForKey("max_manpower").getDouble() * 1000.0)
                : 0;
    }

    @Override
    protected String getDisplayValue() {
        return NumberFormatHelper.formatCompact(value) + " / " + NumberFormatHelper.formatCompact(max);
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

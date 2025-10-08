package com.crschnick.pdxu.app.info.vic3;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.gui.game.GameImage;

import javafx.scene.image.Image;

import java.util.List;

public class Vic3LoyalistComp extends Vic3ChannelComp {

    @Override
    protected String getDisplayValue() {
        if (value < 1000) {
            return String.valueOf(Math.round(value));
        }

        return String.valueOf(Math.round(value / 1000)) + "K";
    }

    @Override
    protected List<String> getNames() {
        return List.of("pop_statistics", "trend_loyalists");
    }

    @Override
    protected Image getImage() {
        return GameImage.VIC3_ICON_LOYALISTS;
    }

    @Override
    protected String getTooltip() {
        return AppI18n.get("loyalists");
    }
}

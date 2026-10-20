package com.crschnick.pdxu.app.info.vic2;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.gui.game.GameImage;

import javafx.scene.image.Image;

public class Vic2PrestigeComp extends Vic2CountryValueComp {

    @Override
    protected String getKey() {
        return "prestige";
    }

    @Override
    protected Image getImage() {
        return GameImage.VIC2_ICON_PRESTIGE;
    }

    @Override
    protected String getTooltip() {
        return AppI18n.get("prestige");
    }
}

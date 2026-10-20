package com.crschnick.pdxu.app.info.vic2;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.util.NumberFormatHelper;

import javafx.scene.image.Image;

public class Vic2PluralityComp extends Vic2CountryValueComp {

    @Override
    protected String getKey() {
        return "plurality";
    }

    @Override
    protected String getDisplayValue() {
        return NumberFormatHelper.formatDecimal(value) + "%";
    }

    @Override
    protected Image getImage() {
        return GameImage.VIC2_ICON_PLURALITY;
    }

    @Override
    protected String getTooltip() {
        return AppI18n.get("plurality");
    }
}

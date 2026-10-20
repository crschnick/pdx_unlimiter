package com.crschnick.pdxu.app.info.vic2;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.gui.game.GameImage;

import javafx.scene.image.Image;

public class Vic2TreasuryComp extends Vic2CountryValueComp {

    @Override
    protected String getKey() {
        return "money";
    }

    @Override
    protected Image getImage() {
        return GameImage.VIC2_ICON_MONEY;
    }

    @Override
    protected String getTooltip() {
        return AppI18n.get("vic3Treasury");
    }
}

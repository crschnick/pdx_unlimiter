package com.crschnick.pdxu.app.info.vic2;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.gui.game.GameImage;

import javafx.scene.image.Image;

public class Vic2LeadershipComp extends Vic2CountryValueComp {

    @Override
    protected String getKey() {
        return "leadership";
    }

    @Override
    protected Image getImage() {
        return GameImage.VIC2_ICON_LEADERSHIP;
    }

    @Override
    protected String getTooltip() {
        return AppI18n.get("leadership");
    }
}

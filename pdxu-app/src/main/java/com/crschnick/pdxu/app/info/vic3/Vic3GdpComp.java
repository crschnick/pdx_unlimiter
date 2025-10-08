package com.crschnick.pdxu.app.info.vic3;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.gui.game.GameImage;

import javafx.scene.image.Image;

import java.util.Collections;
import java.util.List;

public class Vic3GdpComp extends Vic3ChannelComp {

    @Override
    protected String getDisplayValue() {
        return Math.round((value / 1000000.0)) + "M";
    }

    @Override
    protected List<String> getNames() {
        return Collections.singletonList("gdp");
    }

    @Override
    protected Image getImage() {
        return GameImage.VIC3_ICON_GDP;
    }

    @Override
    protected String getTooltip() {
        return AppI18n.get("gdp");
    }
}

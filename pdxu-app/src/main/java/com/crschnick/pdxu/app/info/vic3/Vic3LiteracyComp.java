package com.crschnick.pdxu.app.info.vic3;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.gui.game.GameImage;

import javafx.scene.image.Image;

import java.util.List;

public class Vic3LiteracyComp extends Vic3ChannelComp {

    @Override
    protected List<String> getNames() {
        return List.of("literacy");
    }

    @Override
    protected String getDisplayValue() {
        return Math.round(value * 100) + "%";
    }

    @Override
    protected Image getImage() {
        return GameImage.VIC3_ICON_LITERACY;
    }

    @Override
    protected String getTooltip() {
        return AppI18n.get("literacy");
    }
}

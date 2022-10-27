package com.crschnick.pdxu.app.info.vic3;

import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.lang.PdxuI18n;
import javafx.scene.image.Image;

import java.util.List;

public class Vic3RadicalComp extends Vic3ChannelComp {

    @Override
    protected String getDisplayValue() {
        return String.valueOf(Math.round(value / 1000)) + "K";
    }

    @Override
    protected List<String> getNames() {
        return List.of("pop_statistics", "radical_trend");
    }

    @Override
    protected Image getImage() {
        return GameImage.VIC3_ICON_RADICALS;
    }

    @Override
    protected String getTooltip() {
        return PdxuI18n.get("RADICALS");
    }
}

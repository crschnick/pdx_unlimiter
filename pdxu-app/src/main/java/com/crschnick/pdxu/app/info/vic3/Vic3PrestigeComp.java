package com.crschnick.pdxu.app.info.vic3;

import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.lang.PdxuI18n;
import javafx.scene.image.Image;

import java.util.Collections;
import java.util.List;

public class Vic3PrestigeComp extends Vic3ChannelComp {

    @Override
    protected List<String> getNames() {
        return Collections.singletonList("prestige");
    }

    @Override
    protected Image getImage() {
        return GameImage.VIC3_ICON_PRESTIGE;
    }

    @Override
    protected String getTooltip() {
        return PdxuI18n.get("PRESTIGE");
    }

    @Override
    protected String getDisplayValue() {
        return String.valueOf(Math.round(value));
    }
}

package com.crschnick.pdxu.app.info.vic3;

import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.lang.PdxuI18n;
import javafx.scene.image.Image;

import java.util.Collections;
import java.util.List;

public class Vic3SolComp extends Vic3ChannelComp {

    @Override
    protected List<String> getNames() {
        return Collections.singletonList("avgsoltrend");
    }

    @Override
    protected Image getImage() {
        return GameImage.VIC3_ICON_SOL;
    }

    @Override
    protected String getTooltip() {
        return PdxuI18n.get("STANDARD_OF_LIVING");
    }

    @Override
    protected String getDisplayValue() {
        return String.format("%10.1f", value);
    }
}

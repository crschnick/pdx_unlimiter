package com.crschnick.pdxu.app.info.vic3;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.util.NumberFormatHelper;

import javafx.scene.image.Image;

import java.util.List;

public class Vic3LoyalistComp extends Vic3ChannelComp {

    @Override
    protected String getDisplayValue() {
        return NumberFormatHelper.formatCompact(value);
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

package com.crschnick.pdxu.app.info.eu4;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SimpleInfoComp;
import com.crschnick.pdxu.app.util.NumberFormatHelper;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.savegame.SavegameContent;

import javafx.scene.image.Image;

public class Eu4InflationComp extends SimpleInfoComp {

    private double inflation;

    @Override
    protected void init(SavegameContent content, SavegameData<?> data) {
        inflation = content.get()
                .getNodeForKey("countries")
                .getNodeForKey(data.eu4().getTagName())
                .getNodeForKeyIfExistent("inflation")
                .map(Node::getDouble)
                .orElse(0.0);
    }

    @Override
    protected boolean shouldShow() {
        return inflation > 0;
    }

    @Override
    protected String getDisplayValue() {
        return NumberFormatHelper.formatDecimal(inflation) + "%";
    }

    @Override
    protected Image getImage() {
        return GameImage.EU4_ICON_INFLATION;
    }

    @Override
    protected String getTooltip() {
        return AppI18n.get("inflation");
    }
}

package com.crschnick.pdxu.app.info.eu5;

import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SimpleInfoComp;
import com.crschnick.pdxu.app.installation.GameLocalisation;
import com.crschnick.pdxu.app.util.NumberFormatHelper;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.savegame.SavegameContent;

import javafx.scene.image.Image;
import javafx.scene.layout.Region;

public class Eu5InflationComp extends SimpleInfoComp {

    private double inflation;
    private transient String tooltip;

    @Override
    protected void init(SavegameContent content, SavegameData<?> data) {
        var country = content.get()
                .getNodeForKey("countries")
                .getNodeForKey("database")
                .getNodeForKey(data.eu5().getTag().getId() + "");
        inflation = country
                .getNodeForKey("currency_data")
                .getNodeForKeyIfExistent("inflation")
                .map(Node::getDouble)
                .orElse(0.0);
    }

    @Override
    public Region create(SavegameData<?> data) {
        tooltip = GameLocalisation.getLocalisedValue("inflation", data);
        return super.create(data);
    }

    @Override
    protected boolean shouldShow() {
        return inflation > 0;
    }

    @Override
    protected String getDisplayValue() {
        return NumberFormatHelper.formatDecimal(inflation * 100) + "%";
    }

    @Override
    protected Image getImage() {
        return GameImage.EU5_ICON_INFLATION;
    }

    @Override
    protected String getTooltip() {
        return tooltip;
    }
}

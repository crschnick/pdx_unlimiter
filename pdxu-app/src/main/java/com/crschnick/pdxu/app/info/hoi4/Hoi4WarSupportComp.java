package com.crschnick.pdxu.app.info.hoi4;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SimpleInfoComp;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.savegame.SavegameContent;

import javafx.scene.image.Image;

public class Hoi4WarSupportComp extends SimpleInfoComp {

    private double value;

    @Override
    protected void init(SavegameContent content, SavegameData<?> data) {
        value = content.get()
                .getNodeForKey("countries")
                .getNodeForKey(data.hoi4().getTag().getTag())
                .getNodeForKeyIfExistent("war_support")
                .map(Node::getDouble)
                .orElse(0.0);
    }

    @Override
    protected String getDisplayValue() {
        return Math.round(value * 100) + "%";
    }

    @Override
    protected Image getImage() {
        return GameImage.HOI4_ICON_WAR_SUPPORT;
    }

    @Override
    protected String getTooltip() {
        return AppI18n.get("warSupport");
    }
}

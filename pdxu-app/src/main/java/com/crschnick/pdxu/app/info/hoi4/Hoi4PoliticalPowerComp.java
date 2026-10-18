package com.crschnick.pdxu.app.info.hoi4;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SimpleInfoComp;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.savegame.SavegameContent;

import javafx.scene.image.Image;

public class Hoi4PoliticalPowerComp extends SimpleInfoComp {

    private int value;

    @Override
    protected void init(SavegameContent content, SavegameData<?> data) {
        value = content.get()
                .getNodeForKey("countries")
                .getNodeForKey(data.hoi4().getTag().getTag())
                .getNodeForKeyIfExistent("politics")
                .flatMap(p -> p.getNodeForKeyIfExistent("political_power"))
                .map(Node::getDouble)
                .orElse(0.0)
                .intValue();
    }

    @Override
    protected String getDisplayValue() {
        return String.valueOf(value);
    }

    @Override
    protected Image getImage() {
        return GameImage.HOI4_ICON_POLITICAL_POWER;
    }

    @Override
    protected String getTooltip() {
        return AppI18n.get("politicalPower");
    }
}

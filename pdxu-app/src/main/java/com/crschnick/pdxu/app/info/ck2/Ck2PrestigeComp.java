package com.crschnick.pdxu.app.info.ck2;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SimpleInfoComp;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.savegame.SavegameContent;

import javafx.scene.image.Image;

public class Ck2PrestigeComp extends SimpleInfoComp {

    private int value;

    @Override
    protected void init(SavegameContent content, SavegameData<?> data) {
        value = data.ck2()
                .getPlayerCharacter(content)
                .flatMap(c -> c.getNodeForKeyIfExistent("prs"))
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
        return GameImage.CK2_ICON_PRESTIGE;
    }

    @Override
    protected String getTooltip() {
        return AppI18n.get("prestige");
    }
}

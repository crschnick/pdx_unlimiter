package com.crschnick.pdxu.app.info.eu4;

import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SimpleInfoComp;
import com.crschnick.pdxu.app.lang.PdxuI18n;
import com.crschnick.pdxu.io.node.ArrayNode;
import javafx.scene.image.Image;

public class Eu4PrestigeComp extends SimpleInfoComp {

    private int prestige;

    @Override
    protected void init(ArrayNode node, SavegameData<?> data) {
        prestige = (int) node.getNodeForKey("countries").getNodeForKey(data.eu4().getTagName()).getNodeForKey("prestige").getDouble();
    }

    @Override
    protected String getDisplayValue() {
        return String.valueOf(prestige);
    }

    @Override
    protected Image getImage() {
        return GameImage.EU4_ICON_PRESTIGE;
    }

    @Override
    protected String getTooltip() {
        return PdxuI18n.get("PRESTIGE");
    }
}

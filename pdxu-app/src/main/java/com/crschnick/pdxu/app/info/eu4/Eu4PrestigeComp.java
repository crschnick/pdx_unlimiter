package com.crschnick.pdxu.app.info.eu4;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SimpleInfoComp;

import com.crschnick.pdxu.io.savegame.SavegameContent;
import javafx.scene.image.Image;

public class Eu4PrestigeComp extends SimpleInfoComp {

    private int prestige;

    @Override
    protected void init(SavegameContent content, SavegameData<?> data) {
        prestige = (int) content.get().getNodeForKey("countries").getNodeForKey(data.eu4().getTagName()).getNodeForKey("prestige").getDouble();
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
        return AppI18n.get("prestige");
    }
}

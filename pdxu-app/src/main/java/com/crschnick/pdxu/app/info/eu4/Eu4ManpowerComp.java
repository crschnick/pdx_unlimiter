package com.crschnick.pdxu.app.info.eu4;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SimpleInfoComp;

import com.crschnick.pdxu.io.savegame.SavegameContent;
import javafx.scene.image.Image;

public class Eu4ManpowerComp extends SimpleInfoComp {

    private int value;
    private int max;

    @Override
    protected void init(SavegameContent content, SavegameData<?> data) {
        value = (int) content.get().getNodeForKey("countries").getNodeForKey(data.eu4().getTagName()).getNodeForKey("manpower").getDouble();
        max = (int) content.get().getNodeForKey("countries").getNodeForKey(data.eu4().getTagName()).getNodeForKey("max_manpower").getDouble();
    }

    @Override
    protected String getDisplayValue() {
        return value + "k / " + max + "k";
    }

    @Override
    protected Image getImage() {
        return GameImage.EU4_ICON_MANPOWER;
    }

    @Override
    protected String getTooltip() {
        return AppI18n.get("manpower") + " / " + AppI18n.get("maxManpower");
    }
}

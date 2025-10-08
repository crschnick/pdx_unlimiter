package com.crschnick.pdxu.app.info.eu4;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SimpleInfoComp;

import com.crschnick.pdxu.io.savegame.SavegameContent;
import javafx.scene.image.Image;

public class Eu4CustomNationComp extends SimpleInfoComp {

    private boolean customNationInWorld;

    @Override
    protected void init(SavegameContent content, SavegameData<?> data) {
        content.get().getNodeForKey("countries").forEach((k, v) -> {
            if (v.hasKey("custom_nation_points")) {
                customNationInWorld = true;
            }
        });
    }

    @Override
    protected boolean shouldShow() {
        return customNationInWorld;
    }

    @Override
    protected Image getImage() {
        return GameImage.EU4_ICON_CUSTOM_NATION;
    }

    @Override
    protected String getTooltip() {
        return AppI18n.get("customNation");
    }
}

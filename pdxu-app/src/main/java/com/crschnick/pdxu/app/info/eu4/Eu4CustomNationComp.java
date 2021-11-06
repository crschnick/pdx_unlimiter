package com.crschnick.pdxu.app.info.eu4;

import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SimpleInfoComp;
import com.crschnick.pdxu.app.lang.PdxuI18n;
import com.crschnick.pdxu.io.node.ArrayNode;
import javafx.scene.image.Image;

public class Eu4CustomNationComp extends SimpleInfoComp {

    private boolean customNationInWorld;

    @Override
    protected void init(ArrayNode node, SavegameData<?> data) {
        this.customNationInWorld = data.eu4().getAllTags().stream().anyMatch(t -> t.isCustom());
    }

    @Override
    protected boolean shouldShow() {
        return customNationInWorld;
    }

    @Override
    protected Image getImage() {
        return GameImage.EU4_ICON_RANDOM_NEW_WORLD;
    }

    @Override
    protected String getTooltip() {
        return PdxuI18n.get("RNW");
    }
}

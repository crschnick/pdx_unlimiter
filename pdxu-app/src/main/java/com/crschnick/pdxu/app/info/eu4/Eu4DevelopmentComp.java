package com.crschnick.pdxu.app.info.eu4;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SimpleInfoComp;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.savegame.SavegameContent;

import javafx.scene.image.Image;

public class Eu4DevelopmentComp extends SimpleInfoComp {

    private int autonomyDev;
    private int totalDev;

    @Override
    protected void init(SavegameContent content, SavegameData<?> data) {
        var country = content.get().getNodeForKey("countries").getNodeForKey(data.eu4().getTagName());
        totalDev = country.getNodeForKeyIfExistent("raw_development")
                .map(Node::getDouble)
                .orElse(0.0)
                .intValue();
        autonomyDev = country.getNodeForKeyIfExistent("development")
                .map(Node::getDouble)
                .orElse(0.0)
                .intValue();
    }

    @Override
    protected String getDisplayValue() {
        return autonomyDev + " / " + totalDev;
    }

    @Override
    protected Image getImage() {
        return GameImage.EU4_ICON_DEV;
    }

    @Override
    protected String getTooltip() {
        return AppI18n.get("autonomyDev") + " / " + AppI18n.get("totalDev");
    }
}

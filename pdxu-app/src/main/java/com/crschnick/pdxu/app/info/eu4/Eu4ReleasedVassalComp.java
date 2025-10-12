package com.crschnick.pdxu.app.info.eu4;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SimpleInfoComp;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.savegame.SavegameContent;

import javafx.scene.image.Image;

public class Eu4ReleasedVassalComp extends SimpleInfoComp {

    private boolean releasedVassal;

    @Override
    protected void init(SavegameContent content, SavegameData<?> data) {
        releasedVassal = content.get()
                .getNodeForKey("countries")
                .getNodeForKey(data.eu4().getTag().getTag())
                .getNodeForKeyIfExistent("has_switched_nation")
                .map(Node::getBoolean)
                .orElse(false);
    }

    @Override
    protected boolean shouldShow() {
        return releasedVassal;
    }

    @Override
    protected Image getImage() {
        return GameImage.EU4_ICON_RELEASED_VASSAL;
    }

    @Override
    protected String getTooltip() {
        return AppI18n.get("releasedVassal");
    }
}

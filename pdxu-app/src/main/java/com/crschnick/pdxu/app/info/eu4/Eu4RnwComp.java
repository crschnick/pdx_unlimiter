package com.crschnick.pdxu.app.info.eu4;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SimpleInfoComp;

import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.savegame.SavegameContent;
import javafx.scene.image.Image;

public class Eu4RnwComp extends SimpleInfoComp {

    private boolean rnwEnabled;

    @Override
    public boolean requiresPlayer() {
        return false;
    }

    @Override
    protected boolean shouldShow() {
        return rnwEnabled;
    }


    @Override
    protected void init(SavegameContent content, SavegameData<?> data) {
        rnwEnabled = content.get().getNodeForKeyIfExistent("is_random_new_world").map(Node::getBoolean).orElse(false);
    }

    @Override
    protected Image getImage() {
        return GameImage.EU4_ICON_RANDOM_NEW_WORLD;
    }

    @Override
    protected String getTooltip() {
        return AppI18n.get("rnw");
    }
}

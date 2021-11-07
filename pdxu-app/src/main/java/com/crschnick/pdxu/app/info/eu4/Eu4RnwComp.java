package com.crschnick.pdxu.app.info.eu4;

import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SimpleInfoComp;
import com.crschnick.pdxu.app.lang.PdxuI18n;
import com.crschnick.pdxu.io.node.ArrayNode;
import com.crschnick.pdxu.io.node.Node;
import javafx.scene.image.Image;

public class Eu4RnwComp extends SimpleInfoComp {

    private boolean rnwEnabled;

    @Override
    protected boolean shouldShow() {
        return rnwEnabled;
    }


    @Override
    protected void init(ArrayNode node, SavegameData<?> data) {
        rnwEnabled = node.getNodeForKeyIfExistent("is_random_new_world").map(Node::getBoolean).orElse(false);
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

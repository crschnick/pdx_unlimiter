package com.crschnick.pdxu.app.info;

import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.io.node.ArrayNode;
import javafx.scene.image.Image;

public class Eu4HeirComp extends Eu4RulerComp {

    public Eu4HeirComp(ArrayNode node, SavegameData data) {
        super(node, data);
    }

    @Override
    protected String getRulerKey() {
        return "heir";
    }

    @Override
    protected Image getIcon() {
        return GameImage.EU4_ICON_HEIR;
    }

    @Override
    protected Ruler getDefault() {
        return null;
    }
}

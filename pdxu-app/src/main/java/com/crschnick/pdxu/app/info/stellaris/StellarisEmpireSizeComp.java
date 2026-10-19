package com.crschnick.pdxu.app.info.stellaris;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SimpleInfoComp;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.savegame.SavegameContent;

import javafx.scene.image.Image;

public class StellarisEmpireSizeComp extends SimpleInfoComp {

    private int size;

    @Override
    protected void init(SavegameContent content, SavegameData<?> data) {
        size = content.get()
                .getNodeForKeysIfExistent(
                        "country", String.valueOf(data.stellaris().getTag().getId()), "empire_size")
                .map(Node::getInteger)
                .orElse(0);
    }

    @Override
    protected String getDisplayValue() {
        return String.valueOf(size);
    }

    @Override
    protected Image getImage() {
        return GameImage.STELLARIS_ICON_EMPIRE_SIZE;
    }

    @Override
    protected String getTooltip() {
        return AppI18n.get("empireSize");
    }
}

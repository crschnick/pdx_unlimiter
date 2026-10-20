package com.crschnick.pdxu.app.info.ck3;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SimpleInfoComp;
import com.crschnick.pdxu.app.util.NumberFormatHelper;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.savegame.SavegameContent;

import javafx.scene.image.Image;

public class Ck3LeviesComp extends SimpleInfoComp {

    private int levies;

    @Override
    protected void init(SavegameContent content, SavegameData<?> data) {
        levies = content.get()
                .getNodeForKeysIfExistent(
                        "living", String.valueOf(data.ck3().getTag().getId()), "landed_data", "levy")
                .filter(Node::isValue)
                .map(n -> (int) n.getDouble())
                .orElse(0);
    }

    @Override
    protected boolean shouldShow() {
        return levies > 0;
    }

    @Override
    protected String getDisplayValue() {
        return NumberFormatHelper.formatCompact(levies);
    }

    @Override
    protected Image getImage() {
        return GameImage.CK3_ICON_LEVIES;
    }

    @Override
    protected String getTooltip() {
        return AppI18n.get("levies");
    }
}

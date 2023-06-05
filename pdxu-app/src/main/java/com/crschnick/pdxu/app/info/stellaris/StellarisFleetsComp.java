package com.crschnick.pdxu.app.info.stellaris;

import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SimpleInfoComp;
import com.crschnick.pdxu.app.lang.PdxuI18n;
import com.crschnick.pdxu.io.savegame.SavegameContent;
import javafx.scene.image.Image;

public class StellarisFleetsComp extends SimpleInfoComp {

    private int fleets;

    @Override
    protected void init(SavegameContent content, SavegameData<?> data) {
        fleets = (int) content.get().getNodeForKeys("meta_fleets").getDouble();
    }

    @Override
    protected String getDisplayValue() {
        return String.valueOf(fleets);
    }

    @Override
    protected Image getImage() {
        return GameImage.STELLARIS_ICON_FLEETS;
    }

    @Override
    protected String getTooltip() {
        return PdxuI18n.get("FLEETS");
    }
}

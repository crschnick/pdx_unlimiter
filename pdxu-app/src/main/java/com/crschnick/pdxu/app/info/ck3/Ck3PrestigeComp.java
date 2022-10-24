package com.crschnick.pdxu.app.info.ck3;

import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SimpleInfoComp;
import com.crschnick.pdxu.app.lang.PdxuI18n;
import com.crschnick.pdxu.io.savegame.SavegameContent;
import javafx.scene.image.Image;

import static com.crschnick.pdxu.app.gui.game.GameImage.CK3_ICON_PRESTIGE;

public class Ck3PrestigeComp extends SimpleInfoComp {

    private int prestige;

    @Override
    protected void init(SavegameContent content, SavegameData<?> data) {
        prestige = data.ck3().getTag().getPrestige();
    }

    @Override
    protected String getDisplayValue() {
        return String.valueOf(prestige);
    }

    @Override
    protected Image getImage() {
        return CK3_ICON_PRESTIGE;
    }

    @Override
    protected String getTooltip() {
        return PdxuI18n.get("PRESTIGE");
    }
}

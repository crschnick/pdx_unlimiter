package com.crschnick.pdxu.app.info.ck3;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SimpleInfoComp;

import com.crschnick.pdxu.io.savegame.SavegameContent;
import javafx.scene.image.Image;

import static com.crschnick.pdxu.app.gui.game.GameImage.CK3_ICON_PRESTIGE;

public class Ck3PrestigeComp extends SimpleInfoComp {

    private Integer prestige;

    @Override
    protected void init(SavegameContent content, SavegameData<?> data) {
        prestige = data.ck3().getTag() != null ? data.ck3().getTag().getPrestige() : null;
    }

    @Override
    protected String getDisplayValue() {
        return prestige != null ? String.valueOf(prestige) : null;
    }

    @Override
    protected Image getImage() {
        return CK3_ICON_PRESTIGE;
    }

    @Override
    protected String getTooltip() {
        return AppI18n.get("prestige");
    }
}

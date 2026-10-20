package com.crschnick.pdxu.app.info.ck3;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.model.ck3.Ck3Title;

import javafx.scene.image.Image;

import java.util.List;

public class Ck3ClaimsComp extends Ck3TitleRowComp {

    @Override
    protected List<Ck3Title> getTitles(SavegameData<?> data) {
        return data.ck3().getTag().getClaims();
    }

    @Override
    protected String getStyleClass() {
        return "claims";
    }

    @Override
    protected String getIconTooltip(SavegameData<?> data) {
        return AppI18n.get("claims");
    }

    @Override
    protected Image getIcon() {
        return GameImage.CK3_ICON_CLAIMS;
    }
}

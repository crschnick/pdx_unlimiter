package com.crschnick.pdxu.app.info.ck3;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.gui.game.Ck3CoatOfArmsCache;
import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.DiplomacyRowComp;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.installation.GameFileContext;

import com.crschnick.pdxu.io.savegame.SavegameContent;
import com.crschnick.pdxu.model.ck3.Ck3Title;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;

import static com.crschnick.pdxu.app.gui.GuiStyle.CLASS_TAG_ICON;
import static com.crschnick.pdxu.app.gui.game.GameImage.CK3_ICON_CLAIMS;

public class Ck3ClaimsComp extends DiplomacyRowComp<Ck3Title> {

    @Override
    protected Region map(SavegameData<?> data, Ck3Title tag) {
        return GameImage.imageNode(
                Ck3CoatOfArmsCache.titleImage(tag, GameFileContext.fromData(data)),
                CLASS_TAG_ICON
        );
    }

    @Override
    protected String mapTooltip(SavegameData<?> data, Ck3Title tag) {
        return tag.getName();
    }

    @Override
    protected final void init(SavegameContent content, SavegameData<?> data) {
        this.tags = data.ck3().getTag().getClaims();
    }

    @Override
    protected String getStyleClass() {
        return "claims";
    }

    @Override
    protected String getTooltip() {
        return AppI18n.get("claims");
    }

    @Override
    protected Image getIcon() {
        return CK3_ICON_CLAIMS;
    }
}

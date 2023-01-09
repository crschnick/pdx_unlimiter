package com.crschnick.pdxu.app.info.ck3;

import com.crschnick.pdxu.app.gui.game.Ck3TagRenderer;
import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.DiplomacyRowComp;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.installation.GameFileContext;
import com.crschnick.pdxu.app.lang.PdxuI18n;
import com.crschnick.pdxu.io.savegame.SavegameContent;
import com.crschnick.pdxu.model.ck3.Ck3Tag;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;

import java.util.ArrayList;

import static com.crschnick.pdxu.app.gui.GuiStyle.CLASS_TAG_ICON;
import static com.crschnick.pdxu.app.gui.game.GameImage.CK3_ICON_ALLY;

public class Ck3AlliesComp extends DiplomacyRowComp<Ck3Tag> {

    @Override
    protected Region map(SavegameData<?> data, Ck3Tag tag) {
        return GameImage.imageNode(
                Ck3TagRenderer.renderRealmImage(tag.getCoatOfArms(), tag.getGovernmentName(), GameFileContext.fromData(data), 64, false),
                CLASS_TAG_ICON
        );
    }

    @Override
    protected String mapTooltip(SavegameData<?> data, Ck3Tag tag) {
        return tag.getName();
    }

    @Override
    protected final void init(SavegameContent content, SavegameData<?> data) {
        this.tags = new ArrayList<>();
        for (var rel : content.get().getNodeForKey("relations").getNodeForKey("active_relations").getNodeArray()) {
            if (!rel.hasKey("alliances")) {
                continue;
            }

            var first = rel.getNodeForKey("first").getLong();
            var second = rel.getNodeForKey("second").getLong();
            if (first == data.ck3().getTag().getId()) {
                Ck3Tag.getTag(data.ck3().getAllTags(), second).ifPresent(t -> tags.add(t));
            }
            if (second == data.ck3().getTag().getId()) {
                Ck3Tag.getTag(data.ck3().getAllTags(), first).ifPresent(t -> tags.add(t));
            }
        }
    }

    @Override
    protected String getStyleClass() {
        return "alliance";
    }

    @Override
    protected String getTooltip() {
        return PdxuI18n.get("ALLIES");
    }

    @Override
    protected Image getIcon() {
        return CK3_ICON_ALLY;
    }
}

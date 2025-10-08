package com.crschnick.pdxu.app.info.ck3;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.info.SavegameData;

import com.crschnick.pdxu.io.savegame.SavegameContent;
import com.crschnick.pdxu.model.ck3.Ck3Tag;
import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.List;

import static com.crschnick.pdxu.app.gui.game.GameImage.CK3_ICON_ALLY;

public class Ck3AlliesComp extends Ck3DiplomacyRowComp {

    @Override
    protected List<Ck3Tag> getTags(SavegameContent content, SavegameData<?> data) {
        var tags = new ArrayList<Ck3Tag>();
        var n = content.get().getNodeForKeysIfExistent("relations", "active_relations");
        if (n.isEmpty()) {
            return tags;
        }

        for (var rel : n.get().getNodeArray()) {
            if (rel.isValue() || !rel.hasKey("alliances")) {
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
        return tags;
    }

    @Override
    protected String getStyleClass() {
        return "alliance";
    }

    @Override
    protected String getTooltip() {
        return AppI18n.get("allies");
    }

    @Override
    protected Image getIcon() {
        return CK3_ICON_ALLY;
    }
}

package com.crschnick.pdxu.app.info.ck3;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.gui.GuiStyle;
import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.io.savegame.SavegameContent;
import com.crschnick.pdxu.model.ck3.Ck3Tag;

import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.List;

public class Ck3AlliesComp extends Ck3DiplomacyRowComp {

    @Override
    protected List<Ck3Tag> getTags(SavegameContent content, SavegameData<?> data) {
        var tags = new ArrayList<Ck3Tag>();
        var relations = content.get().getNodeForKeysIfExistent("relations", "active_relations");
        if (relations.isEmpty()) {
            return tags;
        }

        var playerId = data.ck3().getTag().getId();
        for (var rel : relations.get().getNodeArray()) {
            if (!rel.isArray() || !rel.hasKey("alliances")) {
                continue;
            }

            var first = rel.getNodeForKey("first").getLong();
            var second = rel.getNodeForKey("second").getLong();
            var ally = first == playerId ? second : second == playerId ? first : -1;
            if (ally != -1) {
                Ck3Tag.getTag(data.ck3().getAllTags(), ally)
                        .filter(t -> !tags.contains(t))
                        .ifPresent(tags::add);
            }
        }
        return tags;
    }

    @Override
    protected String getStyleClass() {
        return GuiStyle.CLASS_ALLIANCE;
    }

    @Override
    protected String getIconTooltip(SavegameData<?> data) {
        return AppI18n.get("allies");
    }

    @Override
    protected Image getIcon() {
        return GameImage.CK3_ICON_ALLY;
    }
}

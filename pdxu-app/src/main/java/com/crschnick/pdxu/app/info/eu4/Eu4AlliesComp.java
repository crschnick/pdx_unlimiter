package com.crschnick.pdxu.app.info.eu4;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.gui.GuiStyle;
import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.savegame.SavegameContent;
import com.crschnick.pdxu.model.eu4.Eu4Tag;

import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.List;

public class Eu4AlliesComp extends Eu4DiplomacyRowComp {

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
        return GameImage.EU4_ICON_ALLIANCE;
    }

    @Override
    protected List<Eu4Tag> getTags(SavegameContent content, SavegameData<?> data) {
        var list = new ArrayList<Eu4Tag>();
        var diplomacy = content.get().getNodeForKeyIfExistent("diplomacy");
        if (diplomacy.isEmpty()) {
            return list;
        }

        var player = data.eu4().getTagName();
        for (Node alliance : diplomacy.get().getNodesForKey("alliance")) {
            var first = alliance.getNodeForKey("first").getString();
            var second = alliance.getNodeForKey("second").getString();
            var ally = first.equals(player) ? second : second.equals(player) ? first : null;
            if (ally != null) {
                var tag = Eu4Tag.getTag(data.eu4().getAllTags(), ally);
                if (!list.contains(tag)) {
                    list.add(tag);
                }
            }
        }
        return list;
    }
}

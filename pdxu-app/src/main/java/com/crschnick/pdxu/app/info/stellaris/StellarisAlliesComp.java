package com.crschnick.pdxu.app.info.stellaris;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.gui.GuiStyle;
import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.SavegameData;

import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.savegame.SavegameContent;
import com.crschnick.pdxu.model.stellaris.StellarisTag;
import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.List;

public class StellarisAlliesComp extends StellarisDiplomacyRowComp {

    @Override
    protected String getStyleClass() {
        return GuiStyle.CLASS_ALLIANCE;
    }

    @Override
    protected String getTooltip() {
        return AppI18n.get("allies");
    }

    @Override
    protected Image getIcon() {
        return GameImage.STELLARIS_ICON_ALLIANCE;
    }

    @Override
    protected List<StellarisTag> getTags(SavegameContent content, SavegameData<?> data) {
        var list = new ArrayList<StellarisTag>();
        for (Node alli : content.get().getNodeForKeysIfExistent("country", "0", "relations_manager").map(node -> node.getNodesForKey("relation")).orElse(List.of())) {
            if (alli.getNodeForKeyIfExistent("alliance").map(Node::getBoolean).orElse(false)) {
                var c = alli.getNodeForKey("country").getLong();
                var tag = StellarisTag.getTag(data.stellaris().getAllTags(), c);
                tag.ifPresent(list::add);
            }
        }
        return list;
    }
}

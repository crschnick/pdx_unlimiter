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
    protected String getTooltip() {
        return AppI18n.get("allies");
    }

    @Override
    protected Image getIcon() {
        return GameImage.EU4_ICON_ALLIANCE;
    }

    @Override
    protected List<Eu4Tag> getTags(SavegameContent content, SavegameData<?> data) {
        var list = new ArrayList<Eu4Tag>();
        for (Node alli : content.get().getNodeForKey("diplomacy").getNodesForKey("alliance")) {
            String first = alli.getNodeForKey("first").getString();
            String second = alli.getNodeForKey("second").getString();
            if (first.equals(data.eu4().getTagName())) {
                list.add(Eu4Tag.getTag(data.eu4().getAllTags(), second));
            }
            if (second.equals(data.eu4().getTagName())) {
                list.add(Eu4Tag.getTag(data.eu4().getAllTags(), first));
            }
        }
        return list;
    }
}

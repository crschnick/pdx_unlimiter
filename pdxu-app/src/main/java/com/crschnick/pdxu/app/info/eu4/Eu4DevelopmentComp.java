package com.crschnick.pdxu.app.info.eu4;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.gui.GuiTooltips;
import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SavegameInfoComp;

import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.savegame.SavegameContent;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

import static com.crschnick.pdxu.app.gui.GuiStyle.CLASS_IMAGE_ICON;
import static com.crschnick.pdxu.app.gui.game.GameImage.EU4_ICON_DEV;

public class Eu4DevelopmentComp extends SavegameInfoComp {

    private int autonomyDev;
    private int totalDev;

    @Override
    protected void init(SavegameContent content, SavegameData<?> data) {
        totalDev = content.get().getNodeForKey("countries").getNodeForKey(data.eu4().getTagName()).getNodeForKeyIfExistent("raw_development").map(
                Node::getDouble).orElse(0.0).intValue();
        autonomyDev = content.get()
                .getNodeForKey("countries")
                .getNodeForKey(data.eu4().getTagName())
                .getNodeForKeyIfExistent("development")
                .map(
                        Node::getDouble)
                .orElse(0.0)
                .intValue();
    }

    @Override
    public Region create(SavegameData<?> data) {
        var label = new Label(
                autonomyDev + " / " + totalDev,
                GameImage.imageNode(EU4_ICON_DEV, CLASS_IMAGE_ICON)
        );
        label.setMinWidth(Region.USE_PREF_SIZE);
        label.setEllipsisString("");

        var stack = new StackPane(label);
        stack.setAlignment(Pos.CENTER);
        stack.setMinWidth(label.getPrefWidth());
        GuiTooltips.install(stack, AppI18n.get("autonomyDev") + " / " + AppI18n.get("totalDev"));
        return stack;
    }
}

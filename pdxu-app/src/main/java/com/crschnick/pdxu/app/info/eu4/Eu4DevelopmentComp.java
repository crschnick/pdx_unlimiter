package com.crschnick.pdxu.app.info.eu4;

import com.crschnick.pdxu.app.gui.GuiTooltips;
import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SavegameInfoComp;
import com.crschnick.pdxu.app.lang.PdxuI18n;
import com.crschnick.pdxu.io.node.ArrayNode;
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
    protected void init(ArrayNode node, SavegameData<?> data) {
        totalDev = (int) node.getNodeForKey("countries").getNodeForKey(data.eu4().getTagName()).getNodeForKey("raw_development").getDouble();
        autonomyDev = (int) node.getNodeForKey("countries").getNodeForKey(data.eu4().getTagName()).getNodeForKey("development").getDouble();
    }

    @Override
    public Region create() {
        var label = new Label(autonomyDev + " / " + totalDev,
                GameImage.imageNode(EU4_ICON_DEV, CLASS_IMAGE_ICON));
        label.setMinWidth(Region.USE_PREF_SIZE);
        label.setEllipsisString("");

        var stack = new StackPane(label);
        stack.setAlignment(Pos.CENTER);
        stack.setMinWidth(label.getPrefWidth());
        stack.getStyleClass().add("number");
        GuiTooltips.install(stack, PdxuI18n.get("AUTONOMY_DEV") + " / " + PdxuI18n.get("TOTAL_DEV"));
        return stack;
    }
}

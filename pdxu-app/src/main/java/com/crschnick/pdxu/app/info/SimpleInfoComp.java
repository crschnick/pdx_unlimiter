package com.crschnick.pdxu.app.info;

import com.crschnick.pdxu.app.gui.GuiTooltips;
import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.io.node.ArrayNode;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

import static com.crschnick.pdxu.app.gui.GuiStyle.CLASS_IMAGE_ICON;


public abstract class SimpleInfoComp extends SavegameInfoComp {

    public SimpleInfoComp(ArrayNode node) {
        super(node);
    }

    protected abstract Image getImage();

    protected abstract String getTooltip();

    protected abstract String getDisplayValue();

    @Override
    public Region create() {
        var label = new Label(getDisplayValue(),
                GameImage.imageNode(getImage(), CLASS_IMAGE_ICON));
        label.setMinWidth(Region.USE_PREF_SIZE);
        label.setEllipsisString("");

        var stack = new StackPane(label);
        stack.setAlignment(Pos.CENTER);
        stack.setMinWidth(label.getPrefWidth());
        GuiTooltips.install(stack, getTooltip());
        return stack;
    }
}

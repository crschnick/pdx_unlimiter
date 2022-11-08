package com.crschnick.pdxu.app.info;

import com.crschnick.pdxu.app.gui.GuiTooltips;
import com.crschnick.pdxu.app.gui.game.GameImage;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

import static com.crschnick.pdxu.app.gui.GuiStyle.CLASS_IMAGE_ICON;


public abstract class SimpleInfoComp extends SavegameInfoComp {

    protected abstract Image getImage();

    protected abstract String getTooltip();

    protected String getDisplayValue() {
        return null;
    }

    protected boolean shouldShow() {
        return true;
    }

    @Override
    public Region create(SavegameData<?> data) {
        if (!shouldShow()) {
            return null;
        }

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

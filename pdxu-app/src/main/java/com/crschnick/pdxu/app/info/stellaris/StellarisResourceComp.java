package com.crschnick.pdxu.app.info.stellaris;

import com.crschnick.pdxu.app.gui.GuiTooltips;
import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SavegameInfoComp;
import com.crschnick.pdxu.io.savegame.SavegameContent;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public abstract class StellarisResourceComp extends SavegameInfoComp {

    private StellarisSavegameInfo.Resource resource;

    protected abstract String getResourceName();

    protected String getDisplayValue() {
        return (int) resource.stored + " " + (resource.income > resource.expense ? "+" : "") + (int) (resource.income - resource.expense);
    }

    protected String getTooltip() {
        return "Resource stored / change per month";
    }

    protected abstract Image getImage();

    protected boolean shouldShow() {
        return true;
    }

    @Override
    public Region create(SavegameData<?> data) {
        if (!shouldShow()) {
            return null;
        }

        var box = new VBox(GameImage.imageNode(getImage(), "resource-icon"), new Label(getDisplayValue()));
        box.setMinWidth(Region.USE_PREF_SIZE);
        box.setAlignment(Pos.CENTER);

        var stack = new StackPane(box);
        stack.setAlignment(Pos.CENTER);
        stack.setMinWidth(box.getPrefWidth());
        GuiTooltips.install(stack, getTooltip());
        return stack;
    }

    @Override
    protected void init(SavegameContent content, SavegameData<?> data) {
        var node = content.get().getNodeForKeys("country", "0");
        resource = StellarisSavegameInfo.Resource.parseFromCountryNode(node, getResourceName());
    }
}

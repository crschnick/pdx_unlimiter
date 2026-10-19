package com.crschnick.pdxu.app.info.stellaris;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.gui.GuiTooltips;
import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SavegameInfoComp;
import com.crschnick.pdxu.app.installation.GameLocalisation;
import com.crschnick.pdxu.app.util.NumberFormatHelper;
import com.crschnick.pdxu.io.savegame.SavegameContent;
import com.crschnick.pdxu.model.stellaris.Resource;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public abstract class StellarisResourceComp extends SavegameInfoComp {

    protected Resource resource;

    protected abstract String getResourceName();

    protected abstract Image getImage();

    protected String getDisplayValue() {
        return NumberFormatHelper.formatCompact(resource.getStored()) + " "
                + NumberFormatHelper.formatCompactSigned(resource.getIncome() - resource.getExpense());
    }

    protected boolean shouldShow() {
        return resource != null;
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
        GuiTooltips.install(
                stack,
                GameLocalisation.getLocalisedValue(getResourceName(), data) + "\n"
                        + AppI18n.get("resourceStoredChange"));
        return stack;
    }

    @Override
    protected void init(SavegameContent content, SavegameData<?> data) {
        var node = content.get()
                .getNodeForKeysIfExistent(
                        "country", String.valueOf(data.stellaris().getTag().getId()));
        if (node.isEmpty()) {
            return;
        }

        resource = Resource.parseFromCountryNode(node.get(), getResourceName()).orElse(null);
    }
}

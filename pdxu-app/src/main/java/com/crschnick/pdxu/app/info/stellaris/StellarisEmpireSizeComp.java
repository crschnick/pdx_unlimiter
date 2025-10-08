package com.crschnick.pdxu.app.info.stellaris;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SimpleInfoComp;

import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.savegame.SavegameContent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

public class StellarisEmpireSizeComp extends SimpleInfoComp {

    private int size;

    @Override
    protected void init(SavegameContent content, SavegameData<?> data) {
        size = content.get().getNodeForKeysIfExistent("country", "0", "empire_size").map(Node::getInteger).orElse(0);
    }

    @Override
    public Region create(SavegameData<?> data) {
        var r = super.create(data);
        var img = (ImageView) ((StackPane) ((Label) ((StackPane) r).getChildren().getFirst()).getGraphic()).getChildren().getFirst();
        img.fitWidthProperty().unbind();
        img.fitHeightProperty().unbind();
        img.setFitWidth(25);
        img.setFitHeight(25);
        return r;
    }

    @Override
    protected String getDisplayValue() {
        return String.valueOf(size);
    }

    @Override
    protected Image getImage() {
        return GameImage.STELLARIS_ICON_EMPIRE_SIZE;
    }

    @Override
    protected String getTooltip() {
        return AppI18n.get("empireSize");
    }
}

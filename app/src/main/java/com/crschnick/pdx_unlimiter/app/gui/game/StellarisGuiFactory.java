package com.crschnick.pdx_unlimiter.app.gui.game;

import com.crschnick.pdxu.model.SavegameInfo;
import com.crschnick.pdxu.model.stellaris.StellarisSavegameInfo;
import com.crschnick.pdxu.model.stellaris.StellarisTag;
import com.jfoenix.controls.JFXMasonryPane;
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

public class StellarisGuiFactory extends GameGuiFactory<StellarisTag, StellarisSavegameInfo> {

    @Override
    public Image tagImage(SavegameInfo<StellarisTag> info, StellarisTag tag) {
        return StellarisTagRenderer.createTagImage(info, tag);
    }

    @Override
    public Pane background() {
        var bg = GameImage.backgroundNode(GameImage.STELLARIS_BACKGROUND);
        bg.setOpacity(0.3);
        return bg;
    }

    @Override
    public Background createEntryInfoBackground(SavegameInfo<StellarisTag> info) {
        return new Background(new BackgroundFill(
                Color.GRAY,
                CornerRadii.EMPTY, Insets.EMPTY));
    }

    @Override
    public void fillNodeContainer(SavegameInfo<StellarisTag> info, JFXMasonryPane grid) {
        super.fillNodeContainer(info, grid);
    }
}

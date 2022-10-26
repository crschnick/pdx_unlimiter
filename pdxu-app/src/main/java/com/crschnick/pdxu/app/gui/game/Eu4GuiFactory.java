package com.crschnick.pdxu.app.gui.game;

import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.app.info.eu4.Eu4SavegameInfo;
import com.crschnick.pdxu.app.util.ColorHelper;
import com.crschnick.pdxu.model.eu4.Eu4Tag;
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;

import static com.crschnick.pdxu.app.gui.game.GameImage.EU4_BACKGROUND;

public class Eu4GuiFactory extends GameGuiFactory<Eu4Tag, Eu4SavegameInfo> {

    @Override
    public Image tagImage(SavegameInfo<Eu4Tag> info, Eu4Tag tag) {
        return Eu4TagRenderer.shieldImage(info.getData().eu4(), tag);
    }

    @Override
    public Pane background() {
        var bg = GameImage.backgroundNode(EU4_BACKGROUND);
        bg.setOpacity(0.9);
        return bg;
    }

    @Override
    public Background createEntryInfoBackground(SavegameInfo<Eu4Tag> info) {
        return new Background(new BackgroundFill(
                ColorHelper.withAlpha(ColorHelper.fromGameColor(info.getData().getTag().getMapColor()), 0.33),
                CornerRadii.EMPTY, Insets.EMPTY));
    }
}

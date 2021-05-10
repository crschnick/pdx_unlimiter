package com.crschnick.pdx_unlimiter.app.gui.game;

import com.crschnick.pdx_unlimiter.core.info.SavegameInfo;
import com.crschnick.pdx_unlimiter.core.info.vic2.Vic2SavegameInfo;
import com.crschnick.pdx_unlimiter.core.info.vic2.Vic2Tag;
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import static com.crschnick.pdx_unlimiter.app.gui.game.GameImage.VIC2_BACKGROUND;

public class Vic2GuiFactory extends GameGuiFactory<Vic2Tag, Vic2SavegameInfo> {
    @Override
    public Image tagImage(SavegameInfo<Vic2Tag> info, Vic2Tag tag) {
        return ImageLoader.DEFAULT_IMAGE;
    }

    @Override
    public Pane background() {
        var bg = GameImage.backgroundNode(VIC2_BACKGROUND);
        bg.setOpacity(0.4);
        return bg;
    }

    @Override
    public Background createEntryInfoBackground(SavegameInfo<Vic2Tag> info) {
        return new Background(new BackgroundFill(
                Color.DARKGRAY,
                CornerRadii.EMPTY, Insets.EMPTY));
    }
}

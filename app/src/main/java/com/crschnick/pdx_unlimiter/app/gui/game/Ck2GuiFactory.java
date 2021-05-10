package com.crschnick.pdx_unlimiter.app.gui.game;

import com.crschnick.pdx_unlimiter.core.info.SavegameInfo;
import com.crschnick.pdx_unlimiter.core.info.ck2.Ck2SavegameInfo;
import com.crschnick.pdx_unlimiter.core.info.ck2.Ck2Tag;
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import static com.crschnick.pdx_unlimiter.app.gui.game.GameImage.CK2_BACKGROUND;

public class Ck2GuiFactory extends GameGuiFactory<Ck2Tag, Ck2SavegameInfo> {

    @Override
    public Image tagImage(SavegameInfo<Ck2Tag> info, Ck2Tag tag) {
        return ImageLoader.DEFAULT_IMAGE;
    }

    @Override
    public Pane background() {
        var bg = GameImage.backgroundNode(CK2_BACKGROUND);
        bg.setOpacity(0.25);
        return bg;
    }

    @Override
    public Background createEntryInfoBackground(SavegameInfo<Ck2Tag> info) {
        return new Background(new BackgroundFill(
                Color.DARKGRAY,
                CornerRadii.EMPTY, Insets.EMPTY));
    }
}

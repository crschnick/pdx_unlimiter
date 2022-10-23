package com.crschnick.pdxu.app.gui.game;

import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.app.info.vic3.Vic3SavegameInfo;
import com.crschnick.pdxu.app.util.ImageHelper;
import com.crschnick.pdxu.model.vic3.Vic3Tag;
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import static com.crschnick.pdxu.app.gui.game.GameImage.VIC3_BACKGROUND;

public class Vic3GuiFactory extends GameGuiFactory<Vic3Tag, Vic3SavegameInfo> {


    @Override
    public Image tagImage(SavegameInfo<Vic3Tag> info, Vic3Tag tag) {
        if (tag == null) {
            return ImageHelper.DEFAULT_IMAGE;
        }

        return Vic3CoatOfArmsCache.tagFlag(info, tag);
    }

    @Override
    public Pane background() {
        var bg = GameImage.backgroundNode(VIC3_BACKGROUND);
        bg.setOpacity(0.4);
        return bg;
    }

    @Override
    public Background createEntryInfoBackground(SavegameInfo<Vic3Tag> info) {
        return new Background(new BackgroundFill(
                Color.LIGHTGRAY,// Ck3Backgrounds.getBackgroundColor(info),
                CornerRadii.EMPTY, Insets.EMPTY));
    }
}

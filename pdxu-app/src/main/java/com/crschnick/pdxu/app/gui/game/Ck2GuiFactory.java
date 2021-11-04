package com.crschnick.pdxu.app.gui.game;

import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.app.info.ck2.Ck2SavegameInfo;
import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.app.installation.GameInstallation;
import com.crschnick.pdxu.app.util.ImageHelper;
import com.crschnick.pdxu.model.ck2.Ck2Tag;
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

public class Ck2GuiFactory extends GameGuiFactory<Ck2Tag, Ck2SavegameInfo> {

    @Override
    public Image tagImage(SavegameInfo<Ck2Tag> info, Ck2Tag tag) {
        return ImageHelper.loadImage(GameInstallation.ALL.get(Game.CK2).getInstallDir()
                .resolve("gfx").resolve("flags").resolve(tag.getPrimaryTitle() + ".tga"));
    }

    @Override
    public Pane background() {
        var bg = GameImage.backgroundNode(GameImage.CK2_BACKGROUND);
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

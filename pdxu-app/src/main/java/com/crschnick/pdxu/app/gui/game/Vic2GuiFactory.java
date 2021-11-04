package com.crschnick.pdxu.app.gui.game;

import com.crschnick.pdxu.app.gui.GuiStyle;
import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.app.info.vic2.Vic2SavegameInfo;
import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.app.installation.GameInstallation;
import com.crschnick.pdxu.app.util.ImageHelper;
import com.crschnick.pdxu.model.vic2.Vic2Tag;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

public class Vic2GuiFactory extends GameGuiFactory<Vic2Tag, Vic2SavegameInfo> {

    @Override
    public Image tagImage(SavegameInfo<Vic2Tag> info, Vic2Tag tag) {
        return ImageHelper.loadImage(GameInstallation.ALL.get(Game.VIC2).getInstallDir()
                .resolve("gfx").resolve("flags").resolve(tag.getTagId() + ".tga"));
    }

    @Override
    public Pane createIcon() {
        var icon = GameImage.getGameIcon(Game.VIC2);
        var cut = ImageHelper.cut(icon,
                new Rectangle2D(125, 0, icon.getWidth() - 240, icon.getHeight()));
        return GameImage.imageNode(cut, GuiStyle.CLASS_IMAGE_ICON);
    }

    @Override
    public Pane background() {
        var bg = GameImage.backgroundNode(GameImage.VIC2_BACKGROUND);
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

package com.crschnick.pdx_unlimiter.app.gui.game;

import com.crschnick.pdx_unlimiter.app.installation.Game;
import com.crschnick.pdx_unlimiter.app.installation.GameInstallation;
import com.crschnick.pdxu.model.SavegameInfo;
import com.crschnick.pdxu.model.vic2.Vic2SavegameInfo;
import com.crschnick.pdxu.model.vic2.Vic2Tag;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import static com.crschnick.pdx_unlimiter.app.gui.GuiStyle.CLASS_IMAGE_ICON;
import static com.crschnick.pdx_unlimiter.app.gui.game.GameImage.VIC2_BACKGROUND;

public class Vic2GuiFactory extends GameGuiFactory<Vic2Tag, Vic2SavegameInfo> {

    @Override
    public Image tagImage(SavegameInfo<Vic2Tag> info, Vic2Tag tag) {
        return ImageLoader.loadImage(GameInstallation.ALL.get(Game.VIC2).getInstallDir()
                .resolve("gfx").resolve("flags").resolve(tag.getTagId() + ".tga"));
    }

    @Override
    public Pane createIcon() {
        var icon = GameImage.getGameIcon(Game.VIC2);
        var cut = ImageLoader.cut(icon,
                new Rectangle2D(125, 0, icon.getWidth() - 240, icon.getHeight()));
        return GameImage.imageNode(cut, CLASS_IMAGE_ICON);
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

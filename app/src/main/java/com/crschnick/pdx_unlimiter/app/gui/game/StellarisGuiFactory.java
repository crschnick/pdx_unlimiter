package com.crschnick.pdx_unlimiter.app.gui.game;

import com.crschnick.pdx_unlimiter.app.installation.Game;
import com.crschnick.pdx_unlimiter.app.installation.GameInstallation;
import com.crschnick.pdx_unlimiter.app.util.CascadeDirectoryHelper;
import com.crschnick.pdx_unlimiter.app.util.ColorHelper;
import com.crschnick.pdx_unlimiter.core.info.SavegameInfo;
import com.crschnick.pdx_unlimiter.core.info.stellaris.StellarisSavegameInfo;
import com.crschnick.pdx_unlimiter.core.info.stellaris.StellarisTag;
import com.jfoenix.controls.JFXMasonryPane;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

public class StellarisGuiFactory extends GameGuiFactory<StellarisTag, StellarisSavegameInfo> {

    private static final int IMG_SIZE = 256;

    @Override
    public Image tagImage(SavegameInfo<StellarisTag> info, StellarisTag tag) {
        return stellarisTagNode(Path.of(tag.getBackgroundFile()), tag, info);
    }

    @Override
    public Font font() throws IOException {
        return Font.loadFont(
                Files.newInputStream(GameInstallation.ALL.get(Game.STELLARIS)
                        .getPath().resolve("launcher-assets").resolve("font.ttf")), 12);

    }

    @Override
    public Pane background() {
        return GameImage.backgroundNode(GameImage.STELLARIS_BACKGROUND);
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
        var l = new Label("What info would you like to see in this box? Share your feedback on github!");
        l.setAlignment(Pos.CENTER);
        grid.getChildren().add(l);
    }

    private Image stellarisTagNode(
            Path path, StellarisTag tag, SavegameInfo<StellarisTag> info) {
        BufferedImage i = new BufferedImage(IMG_SIZE, IMG_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics g = i.getGraphics();

        int bgPrimary = ColorHelper.intFromColor(ColorHelper.loadStellarisColors(info)
                .getOrDefault(tag.getBackgroundPrimaryColor(), Color.TRANSPARENT));
        Function<Integer, Integer> customFilter = (Integer rgb) -> {
            if (rgb == 0xFFFF0000) {
                return bgPrimary;
            }
            return rgb;
        };

        var in = CascadeDirectoryHelper.openFile(
                Path.of("flags", "backgrounds").resolve(path), info, GameInstallation.ALL.get(Game.STELLARIS));
        in.map(stream -> ImageLoader.loadAwtImage(stream, customFilter))
                .ifPresent(pattern -> g.drawImage(pattern, 0, 0, IMG_SIZE, IMG_SIZE, null));

        Image icon = null;
        var iconIn = CascadeDirectoryHelper.openFile(
                Path.of("flags", tag.getIconCategory()).resolve(tag.getIconFile()),
                info, GameInstallation.ALL.get(Game.STELLARIS));
        iconIn.map(stream -> ImageLoader.loadAwtImage(stream, null))
                .ifPresent(pattern -> g.drawImage(pattern, 0, 0, IMG_SIZE, IMG_SIZE,
                        new java.awt.Color(0, 0, 0, 0), null));

        return ImageLoader.toFXImage(i);
    }
}

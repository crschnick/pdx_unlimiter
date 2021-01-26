package com.crschnick.pdx_unlimiter.app.gui;

import com.crschnick.pdx_unlimiter.app.game.GameInstallation;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameEntry;
import com.crschnick.pdx_unlimiter.app.util.CascadeDirectoryHelper;
import com.crschnick.pdx_unlimiter.app.util.ColorHelper;
import com.crschnick.pdx_unlimiter.core.data.Ck3Tag;
import com.crschnick.pdx_unlimiter.core.savegame.Ck3SavegameInfo;
import com.crschnick.pdx_unlimiter.core.savegame.SavegameInfo;
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

import static com.crschnick.pdx_unlimiter.app.gui.GameImage.CK3_BACKGROUND;
import static com.crschnick.pdx_unlimiter.app.gui.GuiStyle.CLASS_IMAGE_ICON;

public class Ck3GuiFactory extends GameGuiFactory<Ck3Tag, Ck3SavegameInfo> {

    private static final int IMG_SIZE = 256;

    public Ck3GuiFactory() {
        super(GameInstallation.CK3);
    }

    @Override
    public Image tagImage(SavegameInfo<Ck3Tag> info, Ck3Tag tag) {
        BufferedImage i = new BufferedImage(IMG_SIZE, IMG_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics g = i.getGraphics();
        Ck3Tag.CoatOfArms coa = tag.getPrimaryTitle().getCoatOfArms();

        if (coa.getPatternFile() != null) {
            int pColor1 = coa.getColors().size() > 0 ? ColorHelper.intFromColor(ColorHelper.loadCk3(info)
                    .getOrDefault(coa.getColors().get(0), Color.TRANSPARENT)) : 0;
            int pColor2 = coa.getColors().size() > 1 ? ColorHelper.intFromColor(ColorHelper.loadCk3(info)
                    .getOrDefault(coa.getColors().get(1), Color.TRANSPARENT)) : 0;
            int pColor3 = coa.getColors().size() > 2 ? ColorHelper.intFromColor(ColorHelper.loadCk3(info)
                    .getOrDefault(coa.getColors().get(2), Color.TRANSPARENT)) : 0;
            Function<Integer, Integer> patternFunction = (Integer rgb) -> {
                if (rgb == 0xFFFF0000) {
                    return pColor1;
                }
                if (rgb == 0xFFFFFF00) {
                    return pColor2;
                }
                if (rgb == 0xFFFFFFFF) {
                    return pColor3;
                }

                return rgb;
            };
            var in = CascadeDirectoryHelper.openFile(
                    Path.of("gfx", "coat_of_arms", "patterns").resolve(coa.getPatternFile()),
                    info,
                    GameInstallation.CK3);
            BufferedImage pattern = in.map(stream -> ImageLoader.loadAwtImage(stream, patternFunction)).orElse(null);
            g.drawImage(pattern, 0, 0, IMG_SIZE, IMG_SIZE, null);
        }


        for (var emblem : coa.getEmblems()) {
            int eColor1 = emblem.getColors().size() > 0 ? ColorHelper.intFromColor(ColorHelper.loadCk3(info)
                    .getOrDefault(emblem.getColors().get(0), Color.TRANSPARENT)) : 0;
            int eColor2 = emblem.getColors().size() > 1 ? ColorHelper.intFromColor(ColorHelper.loadCk3(info)
                    .getOrDefault(emblem.getColors().get(1), Color.TRANSPARENT)) : 0;
            int eColor3 = emblem.getColors().size() > 2 ? ColorHelper.intFromColor(ColorHelper.loadCk3(info)
                    .getOrDefault(emblem.getColors().get(2), Color.TRANSPARENT)) : 0;
            Function<Integer, Integer> customFilter = (Integer rgb) -> {
                int alpha = rgb & 0xFF000000;
                double c1 = ((0x00FF0000 & rgb) >> 16) / (double) 0xFF;
                double c2 = ((0x0000FF00 & rgb) >> 8) / (double) 0xFF;
                double c3 = ((0x000000FF & rgb)) / (double) 0xFF;

                if ((rgb & 0xFF000080) == 0xFF000080) {
                    return eColor1;
                }
                if ((rgb & 0xFF00FF00) == 0xFF00FF00) {
                    return eColor2;
                }
                return alpha + (int) (c1 * (eColor1 & 0x00FFFFFF)) + (int) (c2 * (eColor2 & 0x00FFFFFF));
            };

            boolean hasColor = emblem.getColors().size() > 0;
            var in = CascadeDirectoryHelper.openFile(
                    Path.of("gfx", "coat_of_arms",
                            (hasColor ? "colored" : "textured") + "_emblems").resolve(emblem.getFile()),
                    info,
                    GameInstallation.CK3);
            var img = in.map(inputStream -> ImageLoader.loadAwtImage(inputStream, customFilter))
                    .orElse(null);
            for (var instance : emblem.getInstances()) {
                g.drawImage(img,
                        (int) instance.getX() * IMG_SIZE,
                        (int) instance.getY() * IMG_SIZE,
                        (int) instance.getScaleX() * IMG_SIZE,
                        (int) instance.getScaleY() * IMG_SIZE,
                        new java.awt.Color(0, 0, 0, 0),
                        null);
            }
        }
        return ImageLoader.toFXImage(i);
    }

    @Override
    public Font font() throws IOException {
        return Font.loadFont(Files.newInputStream(GameInstallation.CK3.getPath()
                .resolve("launcher").resolve("assets").resolve("fonts").resolve("CormorantGaramond-Regular.ttf")), 12);
    }

    @Override
    public Pane background() {
        var bg = GameImage.backgroundNode(CK3_BACKGROUND);
        bg.setOpacity(0.3);
        return bg;
    }

    @Override
    public Pane createIcon() {
        return GameImage.imageNode(GameImage.CK3_ICON, CLASS_IMAGE_ICON);
    }

    @Override
    public Background createEntryInfoBackground(SavegameEntry<Ck3Tag, Ck3SavegameInfo> entry) {
        return new Background(new BackgroundFill(
                Color.CORAL,
                CornerRadii.EMPTY, Insets.EMPTY));
    }

    @Override
    public void fillNodeContainer(SavegameInfo<Ck3Tag> info, JFXMasonryPane grid) {
        super.fillNodeContainer(info, grid);
        var l = new Label("What info would you like to see in this box? Share your feedback on github!");
        l.setAlignment(Pos.CENTER);
        grid.getChildren().add(l);
    }
}

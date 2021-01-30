package com.crschnick.pdx_unlimiter.app.gui;

import com.crschnick.pdx_unlimiter.app.game.GameInstallation;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameEntry;
import com.crschnick.pdx_unlimiter.app.util.CascadeDirectoryHelper;
import com.crschnick.pdx_unlimiter.app.util.ColorHelper;
import com.crschnick.pdx_unlimiter.core.data.Ck3Tag;
import com.crschnick.pdx_unlimiter.core.savegame.Ck3SavegameInfo;
import com.crschnick.pdx_unlimiter.core.savegame.Eu4SavegameInfo;
import com.crschnick.pdx_unlimiter.core.savegame.SavegameInfo;
import com.jfoenix.controls.JFXMasonryPane;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

import static com.crschnick.pdx_unlimiter.app.gui.GameImage.*;
import static com.crschnick.pdx_unlimiter.app.gui.GuiStyle.*;
import static com.crschnick.pdx_unlimiter.app.gui.GuiStyle.CLASS_POWER_ICON;

public class Ck3GuiFactory extends GameGuiFactory<Ck3Tag, Ck3SavegameInfo> {

    public Ck3GuiFactory() {
        super(GameInstallation.CK3);
    }

    private static Region createRulerLabel(Ck3Tag.Person ruler) {
        VBox box = new VBox();

        box.alignmentProperty().set(Pos.CENTER);
        box.getChildren().add(createRulerStatsNode(ruler));
        box.getStyleClass().add(CLASS_RULER);
        GuiTooltips.install(box, ruler.getFirstName());
        return box;
    }

    private static Region createRulerStatsNode(Ck3Tag.Person ruler) {
        VBox box = new VBox();
        box.setAlignment(Pos.CENTER);
        Label adm = new Label(ruler.getSkills().get(0) + "  ", imageNode(CK3_SKILL_DIPLOMACY, CLASS_POWER_ICON));
        box.getChildren().add(adm);
        return box;
    }

    @Override
    public Image tagImage(SavegameInfo<Ck3Tag> info, Ck3Tag tag) {
        return Ck3TagRenderer.tagImage(info, tag);
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
    public Background createEntryInfoBackground(SavegameInfo<Ck3Tag> info) {
        return new Background(new BackgroundFill(
                Color.CORAL,
                CornerRadii.EMPTY, Insets.EMPTY));
    }

    @Override
    public void fillNodeContainer(SavegameInfo<Ck3Tag> info, JFXMasonryPane grid) {
        super.fillNodeContainer(info, grid);
        grid.getChildren().add(createRulerStatsNode(info.getTag().getRuler()));
    }
}

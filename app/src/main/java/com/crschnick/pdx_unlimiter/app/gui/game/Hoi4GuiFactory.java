package com.crschnick.pdx_unlimiter.app.gui.game;

import com.crschnick.pdx_unlimiter.app.installation.Game;
import com.crschnick.pdx_unlimiter.app.installation.GameInstallation;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameCampaign;
import com.crschnick.pdx_unlimiter.app.util.CascadeDirectoryHelper;
import com.crschnick.pdx_unlimiter.core.info.SavegameInfo;
import com.crschnick.pdx_unlimiter.core.info.hoi4.Hoi4SavegameInfo;
import com.crschnick.pdx_unlimiter.core.info.hoi4.Hoi4Tag;
import com.jfoenix.controls.JFXMasonryPane;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Hoi4GuiFactory extends GameGuiFactory<Hoi4Tag, Hoi4SavegameInfo> {

    @Override
    public Font font() throws IOException {
        return Font.loadFont(
                Files.newInputStream(GameInstallation.ALL.get(Game.HOI4).getPath()
                        .resolve("launcher-assets").resolve("font.ttf")), 12);
    }

    @Override
    public Pane background() {
        var bg = GameImage.backgroundNode(GameImage.HOI4_BACKGROUND);
        bg.setOpacity(0.4);
        return bg;
    }

    @Override
    public Background createEntryInfoBackground(SavegameInfo<Hoi4Tag> info) {
        return new Background(new BackgroundFill(
                Color.DARKGRAY,
                CornerRadii.EMPTY, Insets.EMPTY));
    }

    @Override
    public Image tagImage(SavegameInfo<Hoi4Tag> info, Hoi4Tag tag) {
        return hoi4TagNode(GameImage.getHoi4TagPath(tag), info);
    }

    private Image hoi4TagNode(Path path, SavegameInfo<Hoi4Tag> info) {
        var in = CascadeDirectoryHelper.openFile(path, info);
        return ImageLoader.loadImage(in.orElse(null), null);
    }

    @Override
    public ObservableValue<String> createInfoString(SavegameCampaign<Hoi4Tag, Hoi4SavegameInfo> campaign) {
        SimpleStringProperty prop = new SimpleStringProperty(campaign.getDate().toString());
        campaign.dateProperty().addListener((c, o, n) -> {
            Platform.runLater(() -> prop.set(n.toString()));
        });
        return prop;
    }

    @Override
    public void fillNodeContainer(SavegameInfo<Hoi4Tag> info, JFXMasonryPane grid) {
        super.fillNodeContainer(info, grid);
    }
}

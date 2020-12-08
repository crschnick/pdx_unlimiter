package com.crschnick.pdx_unlimiter.app.gui;

import com.crschnick.pdx_unlimiter.app.game.GameCampaign;
import com.crschnick.pdx_unlimiter.app.game.GameCampaignEntry;
import com.crschnick.pdx_unlimiter.app.game.GameInstallation;
import com.crschnick.pdx_unlimiter.eu4.data.Ck3Tag;
import com.crschnick.pdx_unlimiter.eu4.savegame.Ck3SavegameInfo;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.io.IOException;
import java.nio.file.Files;

import static com.crschnick.pdx_unlimiter.app.gui.GameImage.CK3_BACKGROUND;
import static com.crschnick.pdx_unlimiter.app.gui.GameImage.EU4_BACKGROUND;
import static com.crschnick.pdx_unlimiter.app.gui.GuiStyle.CLASS_IMAGE_ICON;
import static com.crschnick.pdx_unlimiter.app.gui.GuiStyle.CLASS_TAG_ICON;

public class Ck3GuiFactory extends GameGuiFactory<Ck3Tag, Ck3SavegameInfo> {

    public Ck3GuiFactory() {
        super(GameInstallation.CK3);
    }

    @Override
    public Font font() throws IOException {
        return Font.loadFont(Files.newInputStream(GameInstallation.CK3.getPath()
                .resolve("launcher").resolve("assets").resolve("fonts").resolve("CormorantGaramond-Regular.ttf")), 12);
    }

    @Override
    public Pane background() {
        var bg = GameImage.backgroundNode(CK3_BACKGROUND);
        bg.setOpacity(0.5);
        return bg;
    }

    @Override
    public Pane createIcon() {
        return GameImage.imageNode(GameImage.CK3_ICON, CLASS_IMAGE_ICON);
    }

    @Override
    public Background createEntryInfoBackground(GameCampaignEntry<Ck3Tag, Ck3SavegameInfo> entry) {
        return new Background(new BackgroundFill(
                Color.ALICEBLUE,
                CornerRadii.EMPTY, Insets.EMPTY));
    }

    @Override
    public ObservableValue<Node> createImage(GameCampaignEntry<Ck3Tag, Ck3SavegameInfo> entry) {
        SimpleObjectProperty<Node> prop = new SimpleObjectProperty<>(GameImage.ck3TagNode(entry.getTag(), CLASS_TAG_ICON));
        entry.infoProperty().addListener((c, o, n) -> {
            Platform.runLater(() -> {
                //prop.set(GameImage.eu4TagNode(entry, CLASS_TAG_ICON));
            });
        });
        return prop;
    }

    @Override
    public ObservableValue<Node> createImage(GameCampaign<Ck3Tag, Ck3SavegameInfo> campaign) {
        SimpleObjectProperty<Node> prop = new SimpleObjectProperty<>(GameImage.ck3TagNode(campaign.getTag(), CLASS_TAG_ICON));
        campaign.tagProperty().addListener((c, o, n) -> {
            //Platform.runLater(() -> prop.set(GameImage.hoi4TagNode(campaign.getTag(), CLASS_TAG_ICON)));
        });
        return prop;
    }
}
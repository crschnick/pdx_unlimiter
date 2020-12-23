package com.crschnick.pdx_unlimiter.app.gui;

import com.crschnick.pdx_unlimiter.app.game.GameCampaign;
import com.crschnick.pdx_unlimiter.app.game.GameCampaignEntry;
import com.crschnick.pdx_unlimiter.app.game.GameInstallation;
import com.crschnick.pdx_unlimiter.app.util.CascadeDirectoryHelper;
import com.crschnick.pdx_unlimiter.app.util.ColorHelper;
import com.crschnick.pdx_unlimiter.core.data.Hoi4Tag;
import com.crschnick.pdx_unlimiter.core.savegame.Hoi4SavegameInfo;
import com.jfoenix.controls.JFXMasonryPane;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.crschnick.pdx_unlimiter.app.gui.GameImage.unknownTag;
import static com.crschnick.pdx_unlimiter.app.gui.GuiStyle.CLASS_IMAGE_ICON;
import static com.crschnick.pdx_unlimiter.app.gui.GuiStyle.CLASS_TAG_ICON;

public class Hoi4GuiFactory extends GameGuiFactory<Hoi4Tag, Hoi4SavegameInfo> {


    public Hoi4GuiFactory() {
        super(GameInstallation.HOI4);
    }

    @Override
    public Font font() throws IOException {
        return Font.loadFont(
                Files.newInputStream(GameInstallation.HOI4.getPath().resolve("launcher-assets").resolve("font.ttf")), 12);
    }

    @Override
    public Pane background() {
        var bg = GameImage.backgroundNode(GameImage.HOI4_BACKGROUND);
        bg.setOpacity(0.4);
        return bg;
    }

    @Override
    public Pane createIcon() {
        return GameImage.imageNode(GameImage.HOI4_ICON, CLASS_IMAGE_ICON);
    }

    @Override
    public Background createEntryInfoBackground(GameCampaignEntry<Hoi4Tag, Hoi4SavegameInfo> entry) {
        return new Background(new BackgroundFill(
                ColorHelper.colorFromInt(0, 100),
                CornerRadii.EMPTY, Insets.EMPTY));
    }

    @Override
    public ObservableValue<Node> createImage(GameCampaignEntry<Hoi4Tag, Hoi4SavegameInfo> entry) {
        SimpleObjectProperty<Node> prop = new SimpleObjectProperty<>(hoi4TagNode(entry));
        entry.infoProperty().addListener((c, o, n) -> {
            Platform.runLater(() -> prop.set(hoi4TagNode(entry)));
        });
        return prop;
    }

    @Override
    public ObservableValue<Node> createImage(GameCampaign<Hoi4Tag, Hoi4SavegameInfo> campaign) {
        SimpleObjectProperty<Node> prop = new SimpleObjectProperty<>(hoi4TagNode(campaign));
        campaign.tagProperty().addListener((c, o, n) -> {
            Platform.runLater(() -> prop.set(hoi4TagNode(campaign)));
        });
        return prop;
    }

    @Override
    public ObservableValue<String> createInfoString(GameCampaign<Hoi4Tag, Hoi4SavegameInfo> campaign) {
        SimpleStringProperty prop = new SimpleStringProperty(campaign.getDate().toString());
        campaign.dateProperty().addListener((c, o, n) -> {
            Platform.runLater(() -> prop.set(n.toString()));
        });
        return prop;
    }

    @Override
    public void fillNodeContainer(GameCampaignEntry<Hoi4Tag, Hoi4SavegameInfo> entry, JFXMasonryPane grid) {
        super.fillNodeContainer(entry, grid);
        var l = new Label("What info would you like to see in this box? Share your feedback on github!");
        l.setAlignment(Pos.CENTER);
        grid.getChildren().add(l);
    }

    private Pane hoi4TagNode(GameCampaign<Hoi4Tag, Hoi4SavegameInfo> campaign) {
        return hoi4TagNode(GameImage.getEu4TagPath(campaign.getTag().getTag()), null);
    }

    private Pane hoi4TagNode(GameCampaignEntry<Hoi4Tag, Hoi4SavegameInfo> entry) {
        return hoi4TagNode(GameImage.getEu4TagPath(entry.getTag().getTag()), entry);
    }

    private Pane hoi4TagNode(Path path, GameCampaignEntry<Hoi4Tag, Hoi4SavegameInfo> entry) {
        var in = CascadeDirectoryHelper.openFile(
                path, entry, GameInstallation.EU4);
        Image img = in.flatMap(inputStream -> ImageLoader.loadImageOptional(inputStream, null)).orElse(null);
        if (img == null) {
            return unknownTag();
        }

        return GameImage.imageNode(img, CLASS_TAG_ICON);
    }
}

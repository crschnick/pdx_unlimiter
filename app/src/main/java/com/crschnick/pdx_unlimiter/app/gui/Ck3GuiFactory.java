package com.crschnick.pdx_unlimiter.app.gui;

import com.crschnick.pdx_unlimiter.app.game.GameCampaign;
import com.crschnick.pdx_unlimiter.app.game.GameCampaignEntry;
import com.crschnick.pdx_unlimiter.app.game.GameInstallation;
import com.crschnick.pdx_unlimiter.app.util.CascadeDirectoryHelper;
import com.crschnick.pdx_unlimiter.app.util.ColorHelper;
import com.crschnick.pdx_unlimiter.core.data.Ck3Tag;
import com.crschnick.pdx_unlimiter.core.savegame.Ck3SavegameInfo;
import com.jfoenix.controls.JFXMasonryPane;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

import static com.crschnick.pdx_unlimiter.app.gui.GameImage.CK3_BACKGROUND;
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
        bg.setOpacity(0.3);
        return bg;
    }

    @Override
    public Pane createIcon() {
        return GameImage.imageNode(GameImage.CK3_ICON, CLASS_IMAGE_ICON);
    }

    @Override
    public Background createEntryInfoBackground(GameCampaignEntry<Ck3Tag, Ck3SavegameInfo> entry) {
        return new Background(new BackgroundFill(
                Color.CORAL,
                CornerRadii.EMPTY, Insets.EMPTY));
    }

    @Override
    public ObservableValue<Node> createImage(GameCampaignEntry<Ck3Tag, Ck3SavegameInfo> entry) {
        SimpleObjectProperty<Node> prop = new SimpleObjectProperty<>(ck3TagNode(entry.getTag(), CLASS_TAG_ICON));
        entry.infoProperty().addListener((c, o, n) -> {
            Platform.runLater(() -> prop.set(ck3TagNode(entry.getTag(), CLASS_TAG_ICON)));
        });
        return prop;
    }

    @Override
    public ObservableValue<Node> createImage(GameCampaign<Ck3Tag, Ck3SavegameInfo> campaign) {
        SimpleObjectProperty<Node> prop = new SimpleObjectProperty<>(ck3TagNode(campaign.getTag(), CLASS_TAG_ICON));
        campaign.tagProperty().addListener((c, o, n) -> {
            Platform.runLater(() -> prop.set(ck3TagNode(campaign.getTag(), CLASS_TAG_ICON)));
        });
        return prop;
    }

    @Override
    public void fillNodeContainer(GameCampaignEntry<Ck3Tag, Ck3SavegameInfo> entry, JFXMasonryPane grid) {
        super.fillNodeContainer(entry, grid);
        var l = new Label("What info would you like to see in this box? Share your feedback on github!");
        l.setAlignment(Pos.CENTER);
        grid.getChildren().add(l);
    }

    private Pane ck3TagNode(Ck3Tag tag, String styleClass) {
        return ck3TagNode(tag.getPrimaryTitle().getCoatOfArms(), null, styleClass);
    }

    private Pane ck3TagNode(
            Ck3Tag.CoatOfArms coa, GameCampaignEntry<Ck3Tag, Ck3SavegameInfo> entry, String styleClass) {
        Image pattern = null;

        {
            int pColor1 = coa.getColors().size() > 0 ? ColorHelper.intFromColor(ColorHelper.loadCk3(entry)
                    .getOrDefault(coa.getColors().get(0), Color.TRANSPARENT)) : 0;
            int pColor2 = coa.getColors().size() > 1 ? ColorHelper.intFromColor(ColorHelper.loadCk3(entry)
                    .getOrDefault(coa.getColors().get(1), Color.TRANSPARENT)) : 0;
            int pColor3 = coa.getColors().size() > 2 ? ColorHelper.intFromColor(ColorHelper.loadCk3(entry)
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
                    entry,
                    GameInstallation.CK3);
            pattern = in.map(inputStream ->
                    ImageLoader.loadImageOptional(inputStream, patternFunction).orElse(null))
                    .orElse(null);
        }


        Image emblem = null;
        if (coa.getEmblemFile() != null) {
            boolean hasColor = coa.getEmblemColors().size() > 0;
            int eColor1 = coa.getEmblemColors().size() > 0 ? ColorHelper.intFromColor(ColorHelper.loadCk3(entry)
                    .getOrDefault(coa.getEmblemColors().get(0), Color.TRANSPARENT)) : 0;
            int eColor2 = coa.getEmblemColors().size() > 1 ? ColorHelper.intFromColor(ColorHelper.loadCk3(entry)
                    .getOrDefault(coa.getEmblemColors().get(1), Color.TRANSPARENT)) : 0;
            int eColor3 = coa.getEmblemColors().size() > 2 ? ColorHelper.intFromColor(ColorHelper.loadCk3(entry)
                    .getOrDefault(coa.getEmblemColors().get(2), Color.TRANSPARENT)) : 0;
            Function<Integer, Integer> customFilter = (Integer rgb) -> {
                if (hasColor) {
                    if (rgb == 0xFF800000) {
                        return eColor1;
                    }
                    if (rgb == 0xFF00007F) {
                        return eColor2;
                    }
                    if (rgb == 0xFFFFFFFF) {
                        return eColor3;
                    }
                }
                return rgb;
            };

            var in = CascadeDirectoryHelper.openFile(
                    Path.of("gfx", "coat_of_arms",
                            (hasColor ? "colored" : "textured") + "_emblems").resolve(coa.getEmblemFile()),
                    entry,
                    GameInstallation.CK3);
            emblem = in.map(inputStream ->
                    ImageLoader.loadImageOptional(inputStream, customFilter).orElse(null))
                    .orElse(null);
        }

        var in = CascadeDirectoryHelper.openFile(
                Path.of("gfx", "coat_of_arms", "patterns").resolve(coa.getEmblemFile()),
                entry,
                GameInstallation.CK3);
        in.map(inputStream ->
                ImageLoader.loadImageOptional(inputStream, null).orElse(null))
                .orElse(null);

        ImageView v = new ImageView(pattern);
        ImageView iconV = new ImageView(emblem);
        StackPane pane = new StackPane(v, iconV);
        v.setPreserveRatio(true);
        v.fitWidthProperty().bind(pane.widthProperty());
        v.fitHeightProperty().bind(pane.heightProperty());
        iconV.fitWidthProperty().bind(pane.widthProperty());
        iconV.fitHeightProperty().bind(pane.heightProperty());
        pane.getStyleClass().add(styleClass);
        return pane;
    }
}

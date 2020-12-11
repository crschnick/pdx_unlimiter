package com.crschnick.pdx_unlimiter.app.gui;

import com.crschnick.pdx_unlimiter.app.game.GameCampaign;
import com.crschnick.pdx_unlimiter.app.game.GameCampaignEntry;
import com.crschnick.pdx_unlimiter.app.game.GameInstallation;
import com.crschnick.pdx_unlimiter.app.installation.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.util.CascadeDirectoryHelper;
import com.crschnick.pdx_unlimiter.app.util.ColorHelper;
import com.crschnick.pdx_unlimiter.core.data.StellarisTag;
import com.crschnick.pdx_unlimiter.core.savegame.StellarisSavegameInfo;
import com.jfoenix.controls.JFXMasonryPane;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.SetChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

import static com.crschnick.pdx_unlimiter.app.gui.GuiStyle.CLASS_IMAGE_ICON;
import static com.crschnick.pdx_unlimiter.app.gui.GuiStyle.CLASS_TAG_ICON;

public class StellarisGuiFactory extends GameGuiFactory<StellarisTag, StellarisSavegameInfo> {

    public StellarisGuiFactory() {
        super(GameInstallation.STELLARIS);
    }

    @Override
    public Font font() throws IOException {
        return Font.loadFont(
                Files.newInputStream(GameInstallation.STELLARIS.getPath().resolve("launcher-assets").resolve("font.ttf")), 12);

    }

    @Override
    public Pane background() {
        return GameImage.backgroundNode(GameImage.STELLARIS_BACKGROUND);
    }

    @Override
    public Pane createIcon() {
        return GameImage.imageNode(GameImage.STELLARIS_ICON, CLASS_IMAGE_ICON);
    }

    @Override
    public Background createEntryInfoBackground(GameCampaignEntry<StellarisTag, StellarisSavegameInfo> entry) {
        return new Background(new BackgroundFill(
                Color.GRAY,
                CornerRadii.EMPTY, Insets.EMPTY));
    }

    @Override
    public ObservableValue<Node> createImage(GameCampaignEntry<StellarisTag, StellarisSavegameInfo> entry) {
        SimpleObjectProperty<Node> prop = new SimpleObjectProperty<>(stellarisTagNode(entry, CLASS_TAG_ICON));
        entry.infoProperty().addListener((c, o, n) -> {
            prop.set(stellarisTagNode(entry, CLASS_TAG_ICON));
            Tooltip.install(prop.get(), new Tooltip());
        });
        return prop;
    }

    @Override
    public ObservableValue<Node> createImage(GameCampaign<StellarisTag, StellarisSavegameInfo> campaign) {
        SimpleObjectProperty<Node> prop = new SimpleObjectProperty<>(stellarisTagNode(campaign.getTag(), CLASS_TAG_ICON));
        prop.bind(createImage(campaign.getLatestEntry()));
        campaign.getEntries().addListener((SetChangeListener<? super GameCampaignEntry<StellarisTag, StellarisSavegameInfo>>) c -> {
            prop.bind(createImage(campaign.getLatestEntry()));
        });
        return prop;
    }

    @Override
    public void fillNodeContainer(GameCampaignEntry<StellarisTag, StellarisSavegameInfo> entry, JFXMasonryPane grid) {
        super.fillNodeContainer(entry, grid);
        var l = new Label("What info would you like to see in this box? Share your feedback on github!");
        l.setAlignment(Pos.CENTER);
        grid.getChildren().add(l);
    }

    private Pane stellarisTagNode(GameCampaignEntry<StellarisTag, StellarisSavegameInfo> entry, String styleClass) {
        return stellarisTagNode(Path.of(entry.getTag().getBackgroundFile()), entry.getTag(), entry, styleClass);
    }

    private Pane stellarisTagNode(StellarisTag tag, String styleClass) {
        return stellarisTagNode(Path.of(tag.getBackgroundFile()), tag, null, styleClass);
    }

    private Pane stellarisTagNode(
            Path path, StellarisTag tag, GameCampaignEntry<StellarisTag, StellarisSavegameInfo> entry, String styleClass) {
        Image img = null;

        int bgPrimary = ColorHelper.intFromColor(ColorHelper.loadStellarisColors(entry)
                .getOrDefault(tag.getBackgroundPrimaryColor(), Color.TRANSPARENT));
        Function<Integer, Integer> customFilter = (Integer rgb) -> {
            if (rgb == 0xFFFF0000) {
                return bgPrimary;
            }
            return rgb;
        };

        var in = CascadeDirectoryHelper.openFile(
                Path.of("flags", "backgrounds").resolve(path), entry, GameInstallation.STELLARIS);
        img = in.map(inputStream ->
                ImageLoader.loadImageOptional(inputStream, customFilter).orElse(null)).orElse(null);

        if (img == null) {
            return GameImage.unknownTag();
        }

        Image icon = null;
        var in2 = CascadeDirectoryHelper.openFile(
                Path.of("flags", tag.getIconCategory()).resolve(tag.getIconFile()),
                entry, GameInstallation.STELLARIS);
        icon = in2.map(inputStream ->
                ImageLoader.loadImageOptional(inputStream, null).orElse(null)).orElse(null);

        if (icon == null) {
            return GameImage.unknownTag();
        }

        ImageView v = new ImageView(img);
        ImageView iconV = new ImageView(icon);
        StackPane pane = new StackPane(v, iconV);
        v.fitWidthProperty().bind(pane.widthProperty());
        v.fitHeightProperty().bind(pane.heightProperty());
        iconV.fitWidthProperty().bind(pane.widthProperty());
        iconV.fitHeightProperty().bind(pane.heightProperty());
        pane.getStyleClass().add(styleClass);
        pane.setMaxWidth(Region.USE_PREF_SIZE);
        pane.setMaxHeight(Region.USE_PREF_SIZE);
        return pane;
    }
}

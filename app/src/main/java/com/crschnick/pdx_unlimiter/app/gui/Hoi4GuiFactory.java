package com.crschnick.pdx_unlimiter.app.gui;

import com.crschnick.pdx_unlimiter.app.game.GameInstallation;
import com.crschnick.pdx_unlimiter.app.game.GameIntegration;
import com.crschnick.pdx_unlimiter.app.game.Hoi4Campaign;
import com.crschnick.pdx_unlimiter.app.game.Hoi4CampaignEntry;
import com.jfoenix.controls.JFXMasonryPane;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import static com.crschnick.pdx_unlimiter.app.gui.GuiStyle.*;

public class Hoi4GuiFactory extends GameGuiFactory<Hoi4CampaignEntry, Hoi4Campaign> {


    private static javafx.scene.paint.Color colorFromInt(int c, int alpha) {
        return Color.rgb(c >>> 24, (c >>> 16) & 255, (c >>> 8) & 255, alpha / 255.0);
    }

    @Override
    public Pane createIcon() {
        return GameImage.imageNode(GameImage.HOI4_ICON, CLASS_IMAGE_ICON);
    }

    @Override
    public Background createEntryInfoBackground(Hoi4CampaignEntry entry) {
        return new Background(new BackgroundFill(
                colorFromInt(GameInstallation.HOI4.getCountryColors().getOrDefault(entry.getTag().getTag(), 0), 100),
                CornerRadii.EMPTY, Insets.EMPTY));
    }

    @Override
    public Pane createGameImage(Hoi4Campaign campaign) {
        return GameImage.hoi4TagNode(campaign.getTag(), CLASS_IMAGE_ICON);
    }

    @Override
    public Pane createImage(Hoi4CampaignEntry entry) {
        var icon = GameImage.hoi4TagNode(entry.getTag(), CLASS_TAG_ICON);
        Tooltip.install(icon, new Tooltip(GameInstallation.HOI4.getCountryNames().get(entry.getTag())));
        return icon;
    }

    @Override
    public String createInfoString(Hoi4CampaignEntry entry) {
        return entry.getDate().toString();
    }

    @Override
    public ObservableValue<Pane> createImage(Hoi4Campaign campaign) {
        SimpleObjectProperty<Pane> prop = new SimpleObjectProperty<>(GameImage.hoi4TagNode(campaign.getTag(), CLASS_TAG_ICON));
        campaign.tagProperty().addListener((c,o,n) -> {
            Platform.runLater(() -> prop.set(GameImage.hoi4TagNode(campaign.getTag(), CLASS_TAG_ICON)));
        });
        return prop;
    }

    @Override
    public ObservableValue<String> createInfoString(Hoi4Campaign campaign) {
        SimpleStringProperty prop = new SimpleStringProperty(campaign.getDate().toString());
        campaign.dateProperty().addListener((c,o,n) -> {
            Platform.runLater(() -> prop.set(n.toString()));
        });
        return prop;
    }

    @Override
    public void fillNodeContainer(Hoi4CampaignEntry entry, JFXMasonryPane grid) {
        var l = new Label("What savegame info would you like to see in this box? Share your feedback on github!");
        l.setAlignment(Pos.CENTER);
        grid.getChildren().add(l);
    }
}
